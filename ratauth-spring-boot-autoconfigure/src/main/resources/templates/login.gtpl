layout 'layout.gtpl',
    title: title,
    error: error,
    bodyContents: contents {
      div(class: 'row') {
        div(class: 'col-sm-6 col-md-4 col-md-offset-4') {
          h1(class: 'text-center login-title') {
            yield 'Sign in to continue'
          }
          div(class: 'account-wall') {
            img(class: 'profile-img', src: 'https://lh5.googleusercontent.com/-b0-k99FZlyE/AAAAAAAAAAI/AAAAAAAAAAA/eu7opA4byxI/photo.jpg?sz=120')
            form(class: 'form-signin', role: "form", method: method, action: action) {
              input(class: 'form-control', placeholder: 'Login', name: 'username', required, autofocus)
              input(type: 'password', class: 'form-control', placeholder: 'Password', name: 'password', required)
              input(type: 'hidden', name: 'aud', value: audValue)
              input(type: 'hidden', name: 'scope', value: scope)
              input(type: 'hidden', name: 'client_id', value: clientId)
              input(type: 'hidden', name: 'response_type', value: responseType)
              input(type: 'hidden', name: 'redirect_uri', value: redirectUri)
              button(class: 'btn btn-lg btn-primary btn-block', type: 'submit') {
                yield 'Sign in'
              }
            }
          }
        }
      }
    }