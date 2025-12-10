module V1
  class LogController < ApplicationController
    def track
      render json: {log: "OK"}
    end
  end
end
