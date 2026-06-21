import { useMemo, useState } from 'react'
import type { FichaItem } from '../../types/ficha'
import { ITEM_CATEGORIES } from '../../types/ficha'
import { Section } from './Section'

interface Props {
  itens: FichaItem[]
}

export function InventorySection({ itens }: Props) {
  const categories = useMemo(() => {
    const fromItems = [...new Set(itens.map((i) => i.categoria).filter(Boolean))]
    const merged = [...ITEM_CATEGORIES]
    fromItems.forEach((c) => {
      if (!merged.includes(c as (typeof ITEM_CATEGORIES)[number])) {
        merged.push(c as (typeof ITEM_CATEGORIES)[number])
      }
    })
    return merged.filter((c) => itens.some((i) => i.categoria === c))
  }, [itens])

  const [active, setActive] = useState<string>(categories[0] ?? '')

  const filtered = itens.filter((i) => i.categoria === active)

  return (
    <Section title="Inventário">
      {itens.length === 0 ? (
        <p className="empty-text">Bolsa vazia.</p>
      ) : (
        <>
          <div className="inventory-tabs">
            {categories.map((cat) => (
              <button
                key={cat}
                type="button"
                className={active === cat ? 'tab active' : 'tab'}
                onClick={() => setActive(cat)}
              >
                {cat}
                <span className="tab-count">
                  {itens.filter((i) => i.categoria === cat).length}
                </span>
              </button>
            ))}
          </div>

          <div className="inventory-list">
            {filtered.map((item, i) => (
              <div key={item.id ?? i} className="inventory-item">
                <div>
                  <strong>{item.nome}</strong>
                  {item.codigo && <span className="item-code">{item.codigo}</span>}
                  {item.descricao && <p className="muted">{item.descricao}</p>}
                </div>
                <span className="item-qty">×{item.quantidade}</span>
              </div>
            ))}
          </div>
        </>
      )}
    </Section>
  )
}
