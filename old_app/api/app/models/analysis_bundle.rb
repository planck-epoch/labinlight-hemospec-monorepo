class AnalysisBundle < ApplicationRecord
  validates :name, presence: true
  validates :code, presence: true

  has_and_belongs_to_many :analysis_tests

  max_paginates_per 20

  scope :search_all, ->(text){
    columns = column_names.map {|column| Arel::Nodes::NamedFunction.new("CAST", [arel_table[column.to_sym].as("TEXT")])}
    query = columns.collect {|column| column.matches("%#{text}%") }
    query = query.reduce {|query, condition| query.or(condition).expr }
    where("(#{query.to_sql})")
  }
    validates :default, inclusion: { in: [true, false] }

    before_save :unset_other_defaults, if: :default?

    private

    def unset_other_defaults
      AnalysisBundle.where(default: true).where.not(id: id).update_all(default: false)
    end
end
