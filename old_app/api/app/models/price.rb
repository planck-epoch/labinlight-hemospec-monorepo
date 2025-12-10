class Price < ApplicationRecord
  validates :organization_id, presence: true
  validates :analysis_bundle_id, presence: true
  validates :value, presence: true

  max_paginates_per 20

  belongs_to :organization
  belongs_to :analysis_bundle

  scope :search_all, ->(text){
    columns = column_names.map {|column| Arel::Nodes::NamedFunction.new("CAST", [arel_table[column.to_sym].as("TEXT")])}
    query = columns.collect {|column| column.matches("%#{text}%") }
    query = query.reduce {|query, condition| query.or(condition).expr }
    where("(#{query.to_sql})")
  }
end
