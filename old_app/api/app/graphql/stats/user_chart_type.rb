module Stats
  class UserChartType < Types::BaseObject
    field :day, String, null: false
    field :value, Integer, null: false

    def self.visible?(context)
      super && context[:current_admin]
    end
  end
end
