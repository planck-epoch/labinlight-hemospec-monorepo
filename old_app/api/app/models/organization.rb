class Organization < ApplicationRecord
  validates :name, presence: false

  has_many :devices, dependent: :destroy
  has_many :analyses, dependent: :destroy
  has_many :prices, dependent: :destroy

  max_paginates_per 20

  scope :search_all, ->(text){
    columns = column_names.map {|column| Arel::Nodes::NamedFunction.new("CAST", [arel_table[column.to_sym].as("TEXT")])}
    query = columns.collect {|column| column.matches("%#{text}%") }
    query = query.reduce {|query, condition| query.or(condition).expr }
    where("(#{query.to_sql})")
  }
end
