Rails.application.routes.draw do
  post "/graphql", to: "graphql#execute"
  devise_for :admins, skip: :sessions
  devise_for :users, skip: :sessions

  # TEMP alias routes
  get "/health", to: "v1/health#check"
  post "/analyze", to: "v1/analyze#create"
  post "/log", to: "v1/log#track"
  get "/analysis_bundle", to: "v1/analysis_bundle#get_all"
  get "/device/:serial_number/config", to: "v1/device#get_device_config"
  post "/device/:serial_number/calibrate", to: "v1/device#calibrate"
  # /TEMP alias routes

  namespace :"v1" do
    get "/health", to: "health#check"
    post "/analyze", to: "analyze#create"
    post "/log", to: "log#track"
    get "/analysis_bundle", to: "analysis_bundle#get_all"
    get "/device/:serial_number/config", to: "device#get_device_config"
    post "/device/:serial_number/calibrate", to: "device#calibrate"
  end

  if Rails.env.development?
    mount GraphiQL::Rails::Engine, at: "/graphiql", graphql_path: "/graphql"
    mount LetterOpenerWeb::Engine, at: "/letter_opener"
  end

  # require 'sidekiq/web'
  # require 'sidekiq/cron/web'
  # mount Sidekiq::Web => '/qlipqueue'
  # Sidekiq::Web.use(Rack::Auth::Basic) do |user, password|
  #   # Protect against timing attacks:
  #   # - See https://codahale.com/a-lesson-in-timing-attacks/
  #   # - See https://thisdata.com/blog/timing-attacks-against-string-comparison/
  #   # - Use & (do not use &&) so that it doesn't short circuit.
  #   # - Use digests to stop length information leaking
  #   Rack::Utils.secure_compare(::Digest::SHA256.hexdigest(user), ::Digest::SHA256.hexdigest(ENV["SIDEKIQ_USER"])) &
  #   Rack::Utils.secure_compare(::Digest::SHA256.hexdigest(password), ::Digest::SHA256.hexdigest(ENV["SIDEKIQ_PASSWORD"]))
  # end

  root to: "application#not_found"
end
