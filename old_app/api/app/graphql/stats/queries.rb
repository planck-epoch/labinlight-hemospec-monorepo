module Stats
  module Queries
    def self.included(child_class)
      child_class.field :stats_users, Integer, null: false, description: "Get number of new users from last X days" do
        argument :days, Integer, required: false

        def self.visible?(context)
          super && context[:current_admin]
        end
      end

      child_class.field :stats_users_per_day, [Stats::UserChartType], null: false, description: "Get new users per days from last X days" do
        argument :days, Integer, required: false
      end

      child_class.field :stats_analyses, Integer, null: false, description: "Get number of new analyses from last X days" do
        argument :days, Integer, required: false

        def self.visible?(context)
          super && context[:current_admin]
        end
      end

      child_class.field :stats_analyses_per_day, [Stats::AnalysisChartType], null: false, description: "Get new analyses per days from last X days" do
        argument :days, Integer, required: false
      end
    end

    def stats_users(days: 30)
      authorize_admin!

      users = User.all.where('created_at >= ?', Date.today-days)

      users.count
    end

    def stats_users_per_day(days: 30)
      authorize_admin!

      users = User.all.where('created_at >= ?', Date.today-days.days)
      users = users.order('created_at desc')

      users_grouped = users.group_by { |t| t.created_at.beginning_of_day.to_date }
      (Date.today-days.days..Date.today).map do |day|
        value = 0
        value = users_grouped[day].count if users_grouped[day]

        OpenStruct.new({day: day.strftime("%d/%m/%Y"), value: value })
      end
    end

    def stats_analyses(days: 30)
      authorize_admin!

      analyses = Analysis.all.where('created_at >= ?', Date.today-days)

      analyses.count
    end

    def stats_analyses_per_day(days: 30)
      authorize_admin!

      analyses = Analysis.all.where('created_at >= ?', Date.today-days.days)
      analyses = analyses.order('created_at desc')

      analyses_grouped = analyses.group_by { |t| t.created_at.beginning_of_day.to_date }
      (Date.today-days.days..Date.today).map do |day|
        value = 0
        value = analyses_grouped[day].count if analyses_grouped[day]

        OpenStruct.new({day: day.strftime("%d/%m/%Y"), value: value })
      end
    end
  end
end
