class Sex < ActiveHash::Base
  self.data = [
    {name: 'Male', id: 'M'}, 
    {name: 'Female', id: 'F'}, 
    {name: 'N/A', id: ''}
  ]
end
