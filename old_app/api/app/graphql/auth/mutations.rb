module Auth
  module Mutations
    def self.included(child_class)
      child_class.field :login, Auth::AuthType, null: true do
        description "Login"
        argument :email, String, required: true
        argument :password, String, required: true
        argument :resource_name, String, required: false
      end

      child_class.field :token_login, Auth::AuthType, null: true do
        description "JWT token login"
        argument :resource_name, String, required: false
      end

      child_class.field :logout, GraphQL::Types::Boolean, null: true do
        description "Logout for users"
        argument :resource_name, String, required: false
      end

      child_class.field :update_user, Auth::AuthType, null: true do
        description "Update user"
        argument :password, String, required: false
        argument :passwordConfirmation, String, required: false
        argument :resource_name, String, required: false
      end

      child_class.field :sign_up, Auth::AuthType, null: true do
        description "Sign up for users"
        argument :email, String, required: true
        argument :password, String, required: true
        argument :passwordConfirmation, String, required: true
      end

      child_class.field :send_reset_password_instructions, GraphQL::Types::Boolean, null: true do
        description "Send password reset instructions to users email"
        argument :email, String, required: true
        argument :resource_name, String, required: false
      end

      child_class.field :reset_password, Auth::AuthType, null: true do
        argument :password, String, required: true
        argument :passwordConfirmation, String, required: true
        argument :resetPasswordToken, String, required: true
        argument :resource_name, String, required: false
      end

      ## OTHER FEATURES
      # field :unlock, GraphQL::Types::Boolean, null: false do
      #   argument :unlockToken, String, required: true
      # end
      #
      # field :resend_unlock_instructions, GraphQL::Types::Boolean, null: false do
      #   argument :email, String, required: true
      # end
    end

    def login(email:, password:, resource_name:'user')
      resource = resource_name.classify.constantize
      user = resource.find_for_authentication(email: email)
      raise Exceptions::AuthenticationError if !user

      is_valid_for_auth = user.valid_for_authentication?{
        user.valid_password?(password)
      }

      if is_valid_for_auth
        return user
      else
        raise Exceptions::AuthenticationError
      end
    end

    ## TOKEN-LOGIN
    def token_login(resource_name: 'user')
      resource_symbol = "current_#{resource_name.underscore}".to_sym
      context[resource_symbol]
    end

    ## LOGOUT
    def logout(resource_name:'user')
      resource_symbol = "current_#{resource_name.underscore}".to_sym
      if context[resource_symbol]
        context[resource_symbol].update(jti: SecureRandom.uuid)
        return true
      end
      false
    end

    def update_user(password: '', password_confirmation: '', resource_name: 'user')
      resource_symbol = "current_#{resource_name.underscore}".to_sym
      if context[resource_symbol]
        password = context[resource_symbol].password
        password_confirmation = context[resource_symbol].password_confirmation
      end

      user = context[resource_symbol]
      return nil if !user
      user.update!(
        password: password,
        password_confirmation: password_confirmation
      )
      user
    end

    def sign_up(email:, password:, password_confirmation:)
      User.create!(
        email: email,
        password: password,
        password_confirmation: password_confirmation
      )
    end

    def send_reset_password_instructions(email:, resource_name:'user')
      resource = resource_name.classify.constantize
      user = resource.find_by_email(email)
      return false if !user
      user.send_reset_password_instructions
      true
    end

    def reset_password(password:, password_confirmation:, reset_password_token:, resource_name:'user')
      resource = resource_name.classify.constantize
      user = resource.with_reset_password_token(reset_password_token)
      raise Exceptions::AuthenticationError if !user
      valid_reset = user.reset_password(password, password_confirmation)

      if valid_reset
        return user
      else
        return GraphQL::ExecutionError.new(user.errors.full_messages.join("\n"), extensions: {code: 'AUTH_ERROR'})
      end
    end

    #
    # uncomment for unlock instructions
    #
    # UNLOCK ACCOUNT
    # def unlock(unlock_token:)
    #   user = User.unlock_access_by_token(unlock_token)
    #   return user.id
    # end

    # RESEND UNLOCK INSTRUCTIONS
    # def resend_unlock_instructions(email:)
    #   user = User.find_by_email(email)
    #   return false if !user

    #   user.resend_unlock_instructions
    # end
  end
end

