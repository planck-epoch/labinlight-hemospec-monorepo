class Device < ApplicationRecord
  validates :serial_number, presence: true
  validates :organization_id, presence: true

  belongs_to :device_config

  max_paginates_per 20

  belongs_to :organization

  scope :search_all, ->(text){
    columns = column_names.map {|column| Arel::Nodes::NamedFunction.new("CAST", [arel_table[column.to_sym].as("TEXT")])}
    query = columns.collect {|column| column.matches("%#{text}%") }
    query = query.reduce {|query, condition| query.or(condition).expr }
    where("(#{query.to_sql})")
  }
end
