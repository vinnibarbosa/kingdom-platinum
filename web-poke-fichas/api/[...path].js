const HOP_BY_HOP_HEADERS = new Set([
  'accept-encoding',
  'connection',
  'content-encoding',
  'content-length',
  'host',
  'origin',
  'referer',
  'transfer-encoding',
]);

export default async function handler(request, response) {
  const backendApiUrl = apiBase(process.env.BACKEND_API_URL);
  if (!backendApiUrl) {
    return response.status(503).json({
      message: 'BACKEND_API_URL não foi configurada na Vercel.',
    });
  }

  const path = Array.isArray(request.query.path)
    ? request.query.path.join('/')
    : String(request.query.path ?? '');
  const query = new URLSearchParams();

  for (const [key, value] of Object.entries(request.query)) {
    if (key === 'path' || value === undefined) {
      continue;
    }
    for (const entry of Array.isArray(value) ? value : [value]) {
      query.append(key, String(entry));
    }
  }

  const queryString = query.toString();
  const target = `${backendApiUrl}/${path}${queryString ? `?${queryString}` : ''}`;
  const headers = new Headers();
  for (const [name, value] of Object.entries(request.headers)) {
    if (!HOP_BY_HOP_HEADERS.has(name.toLowerCase()) && value !== undefined) {
      headers.set(name, Array.isArray(value) ? value.join(', ') : value);
    }
  }
  headers.set('x-forwarded-host', request.headers.host ?? '');
  headers.set('x-forwarded-proto', 'https');
  headers.set('accept-encoding', 'identity');

  try {
    const upstream = await fetch(target, {
      method: request.method,
      headers,
      body: requestBody(request),
      redirect: 'manual',
      signal: AbortSignal.timeout(55_000),
    });

    for (const [name, value] of upstream.headers.entries()) {
      if (!HOP_BY_HOP_HEADERS.has(name.toLowerCase()) && name.toLowerCase() !== 'set-cookie') {
        response.setHeader(name, value);
      }
    }

    const cookies = upstream.headers.getSetCookie?.()
      ?? (upstream.headers.get('set-cookie') ? [upstream.headers.get('set-cookie')] : []);
    if (cookies.length) {
      response.setHeader('Set-Cookie', cookies);
    }

    response.setHeader('Cache-Control', 'no-store');
    const body = Buffer.from(await upstream.arrayBuffer());
    return response.status(upstream.status).send(body);
  } catch (error) {
    console.error('Falha ao acessar a API de produção.', error);
    return response.status(502).json({ message: 'A API está temporariamente indisponível.' });
  }
}

function apiBase(value) {
  const base = value?.trim().replace(/\/+$/, '');
  if (!base) {
    return null;
  }
  return base.endsWith('/api') ? base : `${base}/api`;
}

function requestBody(request) {
  if (request.method === 'GET' || request.method === 'HEAD' || request.body === undefined) {
    return undefined;
  }
  if (Buffer.isBuffer(request.body) || typeof request.body === 'string') {
    return request.body;
  }
  return JSON.stringify(request.body);
}
