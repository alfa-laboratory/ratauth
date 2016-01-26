yieldUnescaped '<!DOCTYPE html>'
html(lang:'en') {
  head {
    meta(charset:'utf-8')
    title(title ?: 'RatAuth')
    meta('http-equiv': "Content-Type", content:"text/html; charset=utf-8")
    meta(name: 'viewport', content: 'width=device-width, initial-scale=1.0')
    script(src: 'js/bootstrap.min.js') {}
    link(href: 'css/main.css', rel: 'stylesheet')
    link(href: 'css/bootstrap.css', rel: 'stylesheet')
    link(href: 'css/bootstrap.min.css', rel: 'stylesheet')
    link(href: 'css/bootstrap-theme.min.css', rel: 'stylesheet')
    link(href: 'css/bootstrap-theme.min.css', rel: 'stylesheet')
  }
  body {
    div(class:'container') {
      if (msg) {
        div(class: 'alert alert-info alert-dismissable') {
          button(type: 'button', class: 'close', 'data-dismiss': 'alert', 'aria-hidden':'true', '&times;')
          yield msg
        }
      }
      bodyContents()
    }
  }
}