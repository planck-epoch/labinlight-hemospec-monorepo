<template>
  <q-layout view="lHh Lpr lFf">
    <q-header
      elevated
    >
      <q-toolbar>
        <q-btn
          flat
          dense
          round
          aria-label="Menu"
          icon="menu"
          @click="leftDrawerOpen = !leftDrawerOpen"
        />

        <q-toolbar-title>
          Admin App
        </q-toolbar-title>

        <!-- <div>Quasar v{{ $q.version }}</div> -->
        <div>{{ user.email }}</div>
      </q-toolbar>
    </q-header>

    <q-drawer
      v-model="leftDrawerOpen"
      bordered
      content-class="bg-grey-2"
    >
      <q-layout>
        <q-list>
          <q-item-label header>
            Menu
          </q-item-label>
          <q-item
            clickable
            tag="a"
            to="/"
            exact
          >
            <q-item-section avatar>
              <q-icon name="dashboard" />
            </q-item-section>
            <q-item-section>
              <q-item-label>Dashboard</q-item-label>
            </q-item-section>
          </q-item>

          <div v-if="logged">
            <q-item
              clickable
              tag="a"
              to="/admins"
            >
              <q-item-section avatar>
                <q-icon name="people" />
              </q-item-section>
              <q-item-section>
                <q-item-label>Admins</q-item-label>
                <q-item-label caption>
                  CRUD demo
                </q-item-label>
              </q-item-section>
            </q-item>

            <q-item
              clickable
              tag="a"
              to="/organizations"
            >
              <q-item-section avatar>
                <q-icon name="store" />
              </q-item-section>
              <q-item-section>
                <q-item-label>Organizations</q-item-label>
                <q-item-label caption>
                  Hospitals, Gyms, etc.
                </q-item-label>
              </q-item-section>
            </q-item>

            <q-item
              clickable
              tag="a"
              to="/devices"
            >
              <q-item-section avatar>
                <q-icon name="dns" />
              </q-item-section>
              <q-item-section>
                <q-item-label>Devices</q-item-label>
                <q-item-label caption>
                  Management
                </q-item-label>
              </q-item-section>
            </q-item>

            <q-item
              clickable
              tag="a"
              to="/deviceConfigs"
            >
              <q-item-section avatar>
                <q-icon name="dns" />
              </q-item-section>
              <q-item-section>
                <q-item-label>DeviceConfigs</q-item-label>
                <q-item-label caption>
                  Management
                </q-item-label>
              </q-item-section>
            </q-item>
            
            <q-item
              clickable
              tag="a"
              to="/analysis_tests"
            >
              <q-item-section avatar>
                <q-icon name="settings" />
              </q-item-section>
              <q-item-section>
                <q-item-label>Analysis Tests</q-item-label>
                <q-item-label caption>
                  Available analysis tests
                </q-item-label>
              </q-item-section>
            </q-item>

            <q-item
              clickable
              tag="a"
              to="/analysis_bundles"
            >
              <q-item-section avatar>
                <q-icon name="settings" />
              </q-item-section>
              <q-item-section>
                <q-item-label>Analysis Bundles</q-item-label>
                <q-item-label caption>
                  Available analysis bundles
                </q-item-label>
              </q-item-section>
            </q-item>

            <q-item
              clickable
              tag="a"
              to="/prices"
            >
              <q-item-section avatar>
                <q-icon name="euro" />
              </q-item-section>
              <q-item-section>
                <q-item-label>Prices</q-item-label>
                <q-item-label caption>
                  Manage Analysis Prices
                </q-item-label>
              </q-item-section>
            </q-item>

            <q-separator spaced />

            <q-item
              clickable
              tag="a"
              to="/analyses"
            >
              <q-item-section avatar>
                <q-icon name="analytics" />
              </q-item-section>
              <q-item-section>
                <q-item-label>Analyses</q-item-label>
                <q-item-label caption>
                  Analyses data
                </q-item-label>
              </q-item-section>
            </q-item>
          </div>
        </q-list>

        <q-footer class="bg-grey-2 text-black">
          <q-list>
            <q-item
              clickable
              tag="a"
              to="/reference"
              exact
            >
              <q-item-section avatar>
                <q-icon name="help" />
              </q-item-section>
              <q-item-section>
                <q-item-label>Reference</q-item-label>
              </q-item-section>
            </q-item>
          </q-list>

          <q-list class="bg-grey-4 text-black">
            <div v-if="logged">
              <q-item
                clickable
                tag="a"
                to="/logout"
              >
                <q-item-section avatar>
                  <q-icon name="lock" />
                </q-item-section>
                <q-item-section>
                  <q-item-label>Logout</q-item-label>
                </q-item-section>
              </q-item>
            </div>
            <div v-else>
              <q-item
                clickable
                tag="a"
                to="/login"
              >
                <q-item-section avatar>
                  <q-icon name="lock" />
                </q-item-section>
                <q-item-section>
                  <q-item-label>Login</q-item-label>
                </q-item-section>
              </q-item>
              <q-item
                clickable
                tag="a"
                to="/signup"
              >
                <q-item-section avatar>
                  <q-icon name="lock" />
                </q-item-section>
                <q-item-section>
                  <q-item-label>Sign Up</q-item-label>
                </q-item-section>
              </q-item>
              <q-item
                clickable
                tag="a"
                to="/recover_password"
              >
                <q-item-section avatar>
                  <q-icon name="lock" />
                </q-item-section>
                <q-item-section>
                  <q-item-label>Recover Password</q-item-label>
                </q-item-section>
              </q-item>
            </div>
          </q-list>
        </q-footer>
      </q-layout>
    </q-drawer>

    <q-page-container>
      <router-view />
    </q-page-container>
  </q-layout>
</template>

<script>
  import { mapGetters } from 'vuex'
  import { bodyClassMixin } from '@/mixins/bodyClass'

  export default {
    mixins: [ bodyClassMixin('default') ],
    data: function() {
      return {
        leftDrawerOpen: this.$q.platform.is.desktop,
      }
    },
    computed: {
      ...mapGetters({
        user: 'auth/user',
        logged: 'auth/logged'
      })
    }
  }
</script>

<style>
</style>
