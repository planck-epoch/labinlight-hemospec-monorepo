class AppSchema < GraphQL::Schema
  query(Types::QueryType)
  mutation(Types::MutationType)
end

GraphQL::Errors.configure(AppSchema) do
  rescue_from ActiveRecord::RecordNotFound do |exception|
    GraphQL::ExecutionError.new("Not Found", extensions: {code: 'NOT_FOUND'})
  end

  rescue_from ActiveRecord::RecordInvalid do |exception|
    GraphQL::ExecutionError.new(exception.record.errors.full_messages.join("\n"))
  end

  rescue_from Exceptions::AuthenticationError do |exception, object, arguments, context|
    GraphQL::ExecutionError.new("Authentication Error", extensions: {code: 'AUTH_ERROR'})
  end

  rescue_from StandardError do |exception|
    # GraphQL::ExecutionError.new("Please try to execute the query for this field later")
    GraphQL::ExecutionError.new(exception, extensions: {code: 'INTERNAL_SERVER_ERROR'})
  end
end

