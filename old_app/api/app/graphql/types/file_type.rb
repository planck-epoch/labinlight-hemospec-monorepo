module Types
  class FileType < Types::BaseObject
    field :filename, String, null: false
    field :encodedFile, String, null: false
  end
end
