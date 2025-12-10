class Analysis < ApplicationRecord
  validates :organization_id, presence: true
  validates :analysis_bundle_id, presence: true
  validates :process_number, presence: true
  validates :sex_code, presence: false
  validates :birth_year, presence: true
  validates :payload, presence: true

  max_paginates_per 20

  extend ActiveHash::Associations::ActiveRecordExtensions
  belongs_to :country, foreign_key: :country_code
  belongs_to :sex, foreign_key: :sex_code, optional: true
  belongs_to :organization
  belongs_to :analysis_bundle

  scope :search_all, ->(text){
    columns = column_names.map {|column| Arel::Nodes::NamedFunction.new("CAST", [arel_table[column.to_sym].as("TEXT")])}
    query = columns.collect {|column| column.matches("%#{text}%") }
    query = query.reduce {|query, condition| query.or(condition).expr }
    where("(#{query.to_sql})")
  }
end
