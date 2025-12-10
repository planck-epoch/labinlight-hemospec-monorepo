<template>
  <q-page
    v-if="admin"
    class="q-pa-md"
  >
    <q-list
      bordered
      padding
    >
      <q-item>
        <q-item-section>
          <q-item-label overline>
            Email
          </q-item-label>
          <q-item-label class="q-pa-md">
            {{ admin.email }}
          </q-item-label>
        </q-item-section>
      </q-item>

      <q-separator spaced />

      <q-item v-show="admin.resetPasswordSentAt">
        <q-item-section>
          <q-item-label overline>
            Reset Password Sent At
          </q-item-label>
          <q-item-label class="q-pa-md">
            {{ admin.resetPasswordSentAt }}
          </q-item-label>
        </q-item-section>
      </q-item>

      <q-item v-show="admin.updatedAt">
        <q-item-section>
          <q-item-label overline>
            Updated At
          </q-item-label>
          <q-item-label class="q-pa-md">
            {{ admin.updatedAt }}
          </q-item-label>
        </q-item-section>
      </q-item>

      <q-separator spaced />

      <q-item>
        <q-item-section>
          <q-item-label overline>
            Created At
          </q-item-label>
          <q-item-label class="q-pa-md">
            {{ admin.createdAt }}
          </q-item-label>
        </q-item-section>
      </q-item>
    </q-list>

    <div class="row q-pa-md">
      <div class="col">
        <q-btn
          class=""
          color="negative"
          size="sm"
          @click.native.stop="confirmDelete(admin.id)"
        >
          Delete
        </q-btn>
      </div>

      <div class="col-6" />

      <div class="col">
        <q-btn
          class="float-right"
          color="primary"
          size="sm"
          label="Edit"
          @click.native.stop="editRecord(admin.id)"
        />
      </div>
    </div>

    <q-dialog
      v-model="showDelete"
      persistent
    >
      <q-card>
        <q-card-section class="row items-center">
          <span class="q-ml-sm">Are you sure you want to delete <strong>"{{ admin.email }}"</strong>?</span>
        </q-card-section>

        <q-card-actions align="right">
          <q-btn
            flat
            label="Cancel"
            color="primary"
            @click.native="cancelSelected()"
          />
          <q-btn
            flat
            label="Delete"
            color="negative"
            @click.native="deleteSelected(admin.id)"
          />
        </q-card-actions>
      </q-card>
    </q-dialog>
  </q-page>
</template>

<script>
  import { mapGetters, mapActions } from 'vuex'
  import { showMixin } from '@/mixins/crud/pagination'

  export default {
    name: 'Admin',
    mixins: [ showMixin('admin') ],
    props: {
      id: {
        type: Number,
        default: undefined
      }
    },
    computed: {
      ...mapGetters({
        byId: 'admins/byId'
      }),
      admin() {
        return this.byId(this.id)
      }
    },
    mounted() {
      let that = this
      this.loadById(this.id).then(function(response){
        if (!response) {
          that.$router.push({name: 'NotFound'})
        }
      })
    },
    methods: {
      ...mapActions({
        loadById: 'admins/loadById',
        delete: 'admins/delete'
      })
    }
  }
</script>

<style lang="scss" scoped>
</style>
