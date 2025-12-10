class AdminMailer < ApplicationMailer
  default from: ENV['TRANS_EMAIL']
  default to: ENV['ADMIN_EMAIL']
  layout 'mailer'

  def new_admin(admin_id)
    @email = Admin.find(admin_id).email

    make_bootstrap_mail(subject: 'New Admin Created')
  end
end
