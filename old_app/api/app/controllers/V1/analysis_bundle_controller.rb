module V1
  class AnalysisBundleController < ApplicationController
    def get_all 
      render json: AnalysisBundle.where(enabled: true)
                    .select(:code, :name, :default)
                    .map { |ab| { Value: ab.code, Name: ab.name, Default: ab.default } }
    end 
  end
end