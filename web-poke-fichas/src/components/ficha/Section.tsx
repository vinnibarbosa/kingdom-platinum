import type { ReactNode } from 'react'

interface SectionProps {
  title: string
  action?: ReactNode
  children: ReactNode
}

export function Section({ title, action, children }: SectionProps) {
  return (
    <section className="ficha-section">
      <header className="ficha-section-header">
        <h3>{title}</h3>
        {action}
      </header>
      <div className="ficha-section-body">{children}</div>
    </section>
  )
}
