module Types
  class BaseObject < GraphQL::Schema::Object
    protected
    
    ["user", "admin"].each do |method|
      define_method "authorize_#{method}" do
        context["current_#{method}".to_sym].present?
      end

      define_method "authorize_#{method}!" do
        return true if send("authorize_#{method}")

        raise(Exceptions::AuthenticationError)
      end
    end

    def authorize_user_or_admin!
      return true if (authorize_user || authorize_admin)

      raise(Exceptions::AuthenticationError)
    end
  end
end
