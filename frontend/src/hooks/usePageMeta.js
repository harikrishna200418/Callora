import { useEffect } from 'react'

/**
 * Sets the document title and meta description for the current page.
 * @param {string} title - Page title (will be appended with " — Callora")
 * @param {string} description - Meta description for the page
 */
export default function usePageMeta(title, description) {
  useEffect(() => {
    const fullTitle = title ? `${title} — Callora` : 'Callora — Battery-Aware Communication'
    document.title = fullTitle

    if (description) {
      let meta = document.querySelector('meta[name="description"]')
      if (meta) {
        meta.setAttribute('content', description)
      } else {
        meta = document.createElement('meta')
        meta.name = 'description'
        meta.content = description
        document.head.appendChild(meta)
      }
    }

    return () => {
      document.title = 'Callora — Battery-Aware Communication'
    }
  }, [title, description])
}
