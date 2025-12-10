import gql from 'graphql-tag'

export const DEFAULT_LOGIN_VALUES = {
  email: '',
  password: ''
}

export const fragments = {
  user: gql`
    fragment authFields on Auth {
      email
      token
    }
  `
}

export const GET_ME = gql`
  query me($resourceName: String) {
    me(resourceName: $resourceName) {
      ...authFields
    }
  }
  ${fragments.user}
`

export const LOGIN_MUTATION = gql`
  mutation login($email: String!, $password: String!, $resourceName: String) {
    login(email: $email, password: $password, resourceName: $resourceName) {
      ...authFields
    }
  }
  ${fragments.user}
`

export const LOGOUT_MUTATION = gql`
  mutation logout($resourceName: String) {
    logout(resourceName: $resourceName)
  }
`

export const SIGNUP_MUTATION = gql`
  mutation signUp($email: String!,
                  $password: String!,
                  $passwordConfirmation: String!) {
    signUp(email: $email,
           password: $password,
           passwordConfirmation: $passwordConfirmation) {
      ...authFields
    }
  }
  ${fragments.user}
`

export const SEND_RESET_PASSWORD_MUTATION = gql`
  mutation sendResetPasswordInstructions($email: String!, $resourceName: String) {
    sendResetPasswordInstructions(email: $email, resourceName: $resourceName)
  }
`

export const RESET_PASSWORD_MUTATION = gql`
  mutation resetPassword($resetPasswordToken: String!,
                         $password: String!,
                         $passwordConfirmation: String!,
                         $resourceName: String) {
    resetPassword(resetPasswordToken: $resetPasswordToken,
                  password: $password,
                  passwordConfirmation: $passwordConfirmation,
                  resourceName: $resourceName) {
      ...authFields
    }
  }
  ${fragments.user}
`

