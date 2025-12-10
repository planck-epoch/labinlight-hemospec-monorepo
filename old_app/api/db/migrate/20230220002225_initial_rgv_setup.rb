class InitialRgvSetup < ActiveRecord::Migration[6.0]
  create_table :users do |t|
    ## Database authenticatable
    t.string :email,              null: false, default: ""
    t.string :encrypted_password, null: false, default: ""

    ## Recoverable
    t.string   :reset_password_token
    t.datetime :reset_password_sent_at

    ## Trackable
    t.datetime :current_sign_in_at

    t.string :jti, null: false

    t.timestamps null: false
  end

  add_index :users, :email,                unique: true
  add_index :users, :reset_password_token, unique: true
  add_index :users, ['jti'], name: 'index_users_on_jti', unique: true, using: :btree


  create_table :admins do |t|
    ## Database authenticatable
    t.string :email,              null: false, default: ""
    t.string :encrypted_password, null: false, default: ""

    ## Recoverable
    t.string   :reset_password_token
    t.datetime :reset_password_sent_at

    ## Trackable
    t.datetime :current_sign_in_at
    t.datetime :last_sign_in_at
    t.inet     :current_sign_in_ip
    t.inet     :last_sign_in_ip

    t.string :jti, null: false

    t.timestamps null: false
  end

  add_index :admins, :email,                unique: true
  add_index :admins, :reset_password_token, unique: true
  add_index :admins, ['jti'], name: 'index_admins_on_jti', unique: true, using: :btree
end
