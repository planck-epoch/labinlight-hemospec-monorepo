<template>
  <q-page
    v-if="organization"
    class="q-pa-md"
  >
    <q-list
      bordered
      padding
    >

  
      <q-item v-show="organization.name">
      <q-item-section>
        <q-item-label overline>Name</q-item-label>
        <q-item-label class="q-pa-md">{{organization.name}}</q-item-label>
      </q-item-section>
    </q-item>

      <q-separator spaced />

      <q-item>
        <q-item-section>
          <q-item-label overline>Updated At</q-item-label>
          <q-item-label class="q-pa-md">{{organization.updatedAt}}</q-item-label>
        </q-item-section>
      </q-item>

      <q-separator spaced />

      <q-item>
        <q-item-section>
          <q-item-label overline>Created At</q-item-label>
          <q-item-label class="q-pa-md">{{organization.createdAt}}</q-item-label>
        </q-item-section>
      </q-item>
    </q-list>

    <div class="row q-pa-md">
      <div class="col">
        <q-btn
          class="gt-xs"
          color="negative"
          size="sm"
          @click.native.stop="confirmDelete(organization.id)"
        >
          Delete
        </q-btn>
      </div>

      <div class="col-6"></div>

      <div class="col">
        <q-btn
          class="gt-xs float-right"
          color="primary"
          size="sm"
          label="Edit"
          @click.native.stop="editRecord(organization.id)"
        />
      </div>
    </div>

    <q-dialog
      v-model="showDelete"
      persistent
    >
      <q-card>
        <q-card-section class="row items-center">
          <span class="q-ml-sm">Are you sure you want to delete <strong>"{{organization.name}}"</strong>?</span>
          <span class="q-ml-sm">It will delete all related <strong>devices</strong>, <strong>prices</strong> and <strong>analyses</strong>.</span>
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
            @click.native="deleteSelected(organization.id)"
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
    name: 'Organization',
    mixins: [ showMixin('organization') ],
    props: {
      id: {
        type: String,
        default: ''
      }
    },
    computed: {
      ...mapGetters({
        byId: 'organizations/byId'
      }),
      organization() {
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
        loadById: 'organizations/loadById',
        delete: 'organizations/delete'
      })
    }
  }
</script>

<style lang="scss" scoped>
</style>
