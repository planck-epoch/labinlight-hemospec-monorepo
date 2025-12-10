class AnalysisTest < ApplicationRecord
  validates :name, presence: true
  validates :code, presence: true

  has_and_belongs_to_many :analysis_bundles
  enum test_type: { numeric: 0, boolean: 1 }

  max_paginates_per 20

  scope :search_all, ->(text){
    columns = column_names.map {|column| Arel::Nodes::NamedFunction.new("CAST", [arel_table[column.to_sym].as("TEXT")])}
    query = columns.collect {|column| column.matches("%#{text}%") }
    query = query.reduce {|query, condition| query.or(condition).expr }
    where("(#{query.to_sql})")
  }
end
