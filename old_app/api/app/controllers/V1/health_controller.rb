module V1
  class HealthController < ApplicationController
    def check
      render json: {status: "OK"}
    end
  end
end
