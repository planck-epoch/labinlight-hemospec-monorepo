// Import normalizr library
import {schema} from 'normalizr'

const user = new schema.Entity('users')
export const userSchema = { user: user }
export const usersSchema = { users: { 'collection': [user] } }

const admin = new schema.Entity('admins')
export const adminSchema = { admin: admin }
export const adminsSchema = { admins: { 'collection': [admin] } }
/*** rgv schemas ***/
// analyses schema
const analysis = new schema.Entity('analyses')
export const analysisSchema = { analysis: analysis }
export const analysesSchema = { analyses: { 'collection': [analysis] } }

// prices schema
const price = new schema.Entity('prices')
export const priceSchema = { price: price }
export const pricesSchema = { prices: { 'collection': [price] } }

// analysisTests schema
const analysisTest = new schema.Entity('analysisTests')
export const analysisTestSchema = { analysisTest: analysisTest }
export const analysisTestsSchema = { analysisTests: { 'collection': [analysisTest] } }

// analysisBundles schema
const analysisBundle = new schema.Entity('analysisBundles', { analysisTests: [analysisTest] })
export const analysisBundleSchema = { analysisBundle: analysisBundle }
export const analysisBundlesSchema = { analysisBundles: { 'collection': [analysisBundle] } }

// devices schema
const device = new schema.Entity('devices')
export const deviceSchema = { device: device }
export const devicesSchema = { devices: { 'collection': [device] } }

// device_configs schema
const deviceConfig = new schema.Entity('deviceConfigs')
export const deviceConfigSchema = { deviceConfig: deviceConfig }
export const deviceConfigsSchema = { deviceConfigs: { 'collection': [deviceConfig] } }

// organizations schema
const organization = new schema.Entity('organizations')
export const organizationSchema = { organization: organization }
export const organizationsSchema = { organizations: { 'collection': [organization] } }

