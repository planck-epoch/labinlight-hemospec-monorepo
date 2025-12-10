module V1
  class AnalyzeController < ApplicationController
    include ActionView::Helpers::NumberHelper

    def create
      payload = params[:analyze]
      device = Device.find_by_serial_number(payload["SerialNumber"])

      if device
        analysis_bundle = AnalysisBundle.find_by_code(payload["AnalysisBundle"])

        if analysis_bundle && analysis_bundle.enabled
          analysis = Analysis.new(organization_id: device.organization_id,
                                  analysis_bundle: analysis_bundle,
                                  process_number: payload["ProcessNumber"],
                                  sex_code: payload["Sex"],
                                  birth_year: payload["BirthdayYear"],
                                  country_code: payload["PatientIdCountry"],
                                  health_number: payload["PatientId"],
                                  phone: payload["PatientContactNumber"],
                                  payload: payload)

          uri = URI("#{ENV['PREDICT_URL']}/analyze")
          headers = { 'Content-Type': 'application/json' }
          analysis.payload[:analysis_tests] = analysis_bundle.analysis_tests.map(&:code)
          res = Net::HTTP.post(uri, analysis.payload.to_json, headers)

          if res.is_a?(Net::HTTPSuccess)
            analysis.results = JSON.parse(res.body)
            analysis.save

            results = []
            analysis_bundle.analysis_tests.each do |analysis_test|
                if (analysis.sex_code == 'M')
                  reference_value = analysis_test.reference_male
                elsif (analysis.sex_code == 'F')
                  reference_value = analysis_test.reference_female
                else
                  reference_value = ''
                end
                formatted_value = number_with_precision(analysis.results[analysis_test.code], precision: 2, delimiter: '.', separator: ',', strip_insignificant_zeros: true)
                result = {
                  "Code": analysis_test.code,
                  "Label": analysis_test.name,
                  "Unit": analysis_test.unit,
                  "Value": analysis.results[analysis_test.code] || 0,
                  "ValueString": formatted_value,
                  "ReferenceValues": reference_value
                }
              if analysis_test.test_type == 'boolean' && analysis.results[analysis_test.code]
                result[:ValueString] =  "#{analysis.results[analysis_test.code] < analysis_test.reference_value ? 'Negativo' : 'Positivo'} (estimado #{formatted_value})"
              else
                
              end
              results.push(result)
            end
            
            render json: results
          elsif res.is_a?(Net::HTTPInternalServerError)
            render json: res.body, status: :internal_server_error
          else
            render json: {error: res.body}, status: :not_found
          end
        else
          render json: {error: "Analysis Bundle not available"}, status: :not_found
        end
      else
        render json: {error: "Device not found"}, status: :not_found
      end
    end
  end
end
