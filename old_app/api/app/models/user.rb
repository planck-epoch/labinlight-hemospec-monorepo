class User < ApplicationRecord
  max_paginates_per 20

  include Devise::JWT::RevocationStrategies::JTIMatcher
  include Tokenizable

  # Include default devise modules. Others available are:
  # :confirmable, :lockable, :timeoutable, :trackable and :omniauthable
  devise :database_authenticatable,
         :registerable,
         :recoverable,
         :rememberable,
         :validatable,
         :jwt_authenticatable,
         jwt_revocation_strategy: self


  validates :email, presence: true,
                    length: { maximum: 255 },
                    format: { with: Regex::Email::VALIDATE }

  scope :search_all, ->(text){
    columns = column_names.map {|column| Arel::Nodes::NamedFunction.new("CAST", [arel_table[column.to_sym].as("TEXT")])}
    query = columns.collect {|column| column.matches("%#{text}%") }
    query = query.reduce {|query, condition| query.or(condition).expr }
    where("(#{query.to_sql})")
  }

  # Send mail through activejob
  def send_devise_notification(notification, *args)
    if Rails.env.development?
      devise_mailer.send(notification, self, *args).deliver!
    else
      devise_mailer.send(notification, self, *args).deliver_later
    end
  end
end
