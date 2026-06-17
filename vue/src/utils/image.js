export function resolveImageUrl(url, baseUrl = import.meta.env.VITE_APP_BASE_API) {
  if (!url || /^(data:|blob:)/.test(url)) return url
  if (/^https?:/.test(url)) {
    try {
      const parsedUrl = new URL(url)
      if (parsedUrl.pathname.startsWith('/profile') || parsedUrl.pathname.startsWith('/uploads')) {
        return baseUrl + parsedUrl.pathname
      }
    } catch (e) {
      return url
    }
    return url
  }
  return baseUrl + url
}
