class FixAnalysesAtributes < ActiveRecord::Migration[6.1]
  def change
    remove_column :analyses, :country_id
    add_column :analyses, :country_code, :string

    remove_column :analyses, :sex_id
    add_column :analyses, :sex_code, :string
  end
end
