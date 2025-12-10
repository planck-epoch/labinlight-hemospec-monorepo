module Analyses
  module Queries
    def self.included(child_class)
      child_class.field :analyses, Analyses::AnalysisType.collection_type, null: false, description: "An example field added by the generator" do
        argument :page, Integer, required: false
        argument :limit, Integer, required: false, default_value: 20, prepare: ->(limit, ctx) {[limit, 20].min}
        argument :order, String, required: false
        argument :direction, String, required: false
        argument :search, String, required: false
      end
      child_class.field :analysis, Analyses::AnalysisType, null: false, description: "Find a Analysis by ID" do
        argument :id, Integer, required: true
      end
      child_class.field :analyses_export, Types::FileType, null: false, description: "Export analyses" do
        argument :ids, [Integer], required: false
      end
    end

    def analyses(page: nil, limit: nil, order: nil, direction: nil, search: '')
      authorize_user_or_admin!

      order ||= 'id'
      order = order.underscore
      order = Analysis.column_names.include?(order) ? order :
      direction = direction == 'asc' ? 'asc' : 'desc'

      analyses = Analysis.all.order(Arel.sql("#{order} #{direction}"))
      analyses = analyses.search_all(search) unless search.blank?

      analyses.page(page).per(limit)
    end

    def analysis(id:)
      authorize_user_or_admin!

      Analysis.find(id)
    end

    def analyses_export(ids: [])
      require 'zip'

      authorize_admin!

      if ids.blank?
        analyses = Analysis.all
      else
        analyses = Analysis.where(id: ids)
      end

      temp_dir = Dir.mktmpdir

      analyses.each do |analysis|
        analysis.payload["Samples"].each_with_index do |sample, index|
          dirname = "#{temp_dir}/#{analysis.created_at.strftime("%Y%m%d")}/#{analysis.payload['SerialNumber']}"
          unless File.directory?(dirname)
            FileUtils.mkdir_p(dirname)
          end
          
          csv_data = "Method:,#{sample['Method']},,\n"
          csv_data += "Process Number:,#{analysis.payload['ProcessNumber']},,\n"
          csv_data += "Patient ID:,#{analysis.payload['PatientId']},,\n"
          csv_data += "Patient Country:,#{analysis.payload['PatientIdCountry']},,\n"
          csv_data += "Analysis Bundle:,#{analysis.payload['AnalysisBundle']},,\n"
          csv_data += "Organization Name:,#{analysis.organization.name},,\n"
          csv_data += "Age:,#{analysis.payload['Age']},,\n"
          csv_data += "Sex:,#{analysis.payload['Sex']},,\n"
          csv_data += "DateReading:,#{analysis.payload['DateReading']},,\n"
          csv_data += "Header Version:,#{sample['HeaderVersion']},,\n"
          csv_data += "System Temp (C):,#{sample['SystemTemp']},,\n"
          csv_data += "Detector Temp (C):,#{sample['RefTemp']},,\n"  # RefTemp ou RefDetectorTemp
          csv_data += "Humidity (%):,#{sample['Humidity']},,\n"
          csv_data += "Lamp PD:,#{sample['RefLampPD']},,\n"
          csv_data += "Shift Vector Coefficients:,#{sample['ShiftVectorCoefficients'][0]},#{sample['ShiftVectorCoefficients'][1]},#{sample['ShiftVectorCoefficients'][2]},,\n"
          csv_data += "Pixel to Wavelength Coefficients:,#{sample['PixelWavelengtCoefficients'][0]},#{sample['PixelWavelengtCoefficients'][1]},#{sample['PixelWavelengtCoefficients'][2]},,\n"
          csv_data += "Serial Number:,#{sample['RefSerialNumber']},,\n"
          csv_data += "Scan Config Name:,#{sample['ScanConfigName']},,\n"
          csv_data += "Scan Config Type:,#{sample['ScanConfigType']},,\n"
          csv_data += "Section 1,,\n"
          csv_data += "Start wavelength (nm):,#{sample['StartWavelength']},,\n"
          csv_data += "End wavelength (nm):,#{sample['EndWavelength']},,\n"
          csv_data += "Pattern Pixel Width (nm):,#{sample['PatternPixelWidth']},,,,\n"
          csv_data += "Exposure (ms):,#{sample['Exposure']},,,,\n"
          csv_data += "Digital Resolution:,#{sample['DigitalResolution']},,\n"
          csv_data += "Num Repeats:,#{sample['NumRep']},,\n"
          csv_data += "PGA Gain:,#{sample['PGAgain']},,\n"
          csv_data += "Total Measurement Time in sec:,#{sample['TotalTimeScan']},,\n"
          csv_data += "Wavelength (nm),Absorbance (AU),Reference Signal (unitless),Sample Signal (unitless)\n"

          sample["Absorbance"].each_with_index do |absorbance, index|
            if sample['Intensity'].nil?
              csv_data += "#{sample['WaveLength'][index]},#{sample['Absorbance'][index]},#{sample['ReferenceIntensity'][index]},\n"
            else
              csv_data += "#{sample['WaveLength'][index]},#{sample['Absorbance'][index]},#{sample['ReferenceIntensity'][index]},#{sample['Intensity'][index]}\n"
            end
          end

          filename = ActiveStorage::Filename.new("#{analysis.payload['ProcessNumber']}#{sample['Method']} #{analysis.created_at.strftime("%Y%m%d-%H%M%S")}_#{index+1}.csv").sanitized
          csv_file_path = File.join(dirname, filename)
          File.write(csv_file_path, csv_data)
        end
      end

      # Generate a unique filename for the ZIP file
      zip_filename = "analyses_export_#{Time.now.to_i}.zip"
      zip_file_path = File.join(temp_dir, zip_filename)

      # Create the ZIP file containing all the CSV files
      Zip::File.open(zip_file_path, Zip::File::CREATE) do |zipfile|
        Dir[File.join(temp_dir, '**/**')].each do |file|
          filename = File.basename(file)
          zipfile.add(file.sub(temp_dir+'/', ''), file)
        end
      end

      # Send the ZIP file as a response
      # send_file zip_file_path, filename: zip_filename, disposition: 'attachment'
      data = File.open(zip_file_path).read
      encoded_file = Base64.encode64(data)

      # Clean up the temporary directory and files
      FileUtils.remove_dir(temp_dir, force: true)

      return {
        filename: zip_filename,
        encoded_file: encoded_file
      }
    end
  end
end
