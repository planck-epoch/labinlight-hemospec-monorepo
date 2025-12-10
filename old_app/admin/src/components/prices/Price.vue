<template>
  <q-page
    v-if="price"
    class="q-pa-md"
  >
    <q-list
      bordered
      padding
    >

  
      <q-item v-show="price.organizationId">
      <q-item-section>
        <q-item-label overline>Organization</q-item-label>
        <q-item-label class="q-pa-md">{{price.organization.name}}</q-item-label>
      </q-item-section>
    </q-item>
      <q-separator spaced />
  
      <q-item v-show="price.analysisBundleId">
      <q-item-section>
        <q-item-label overline>Analysis Bundle</q-item-label>
        <q-item-label class="q-pa-md">{{price.analysisBundle.name}}</q-item-label>
      </q-item-section>
    </q-item>
      <q-separator spaced />
  
      <q-item v-show="price.value">
      <q-item-section>
        <q-item-label overline>Value</q-item-label>
        <q-item-label class="q-pa-md">{{price.value}}</q-item-label>
      </q-item-section>
    </q-item>

      <q-separator spaced />

      <q-item>
        <q-item-section>
          <q-item-label overline>Updated At</q-item-label>
          <q-item-label class="q-pa-md">{{price.updatedAt}}</q-item-label>
        </q-item-section>
      </q-item>

      <q-separator spaced />

      <q-item>
        <q-item-section>
          <q-item-label overline>Created At</q-item-label>
          <q-item-label class="q-pa-md">{{price.createdAt}}</q-item-label>
        </q-item-section>
      </q-item>
    </q-list>

    <div class="row q-pa-md">
      <div class="col">
        <q-btn
          class="gt-xs"
          color="negative"
          size="sm"
          @click.native.stop="confirmDelete(price.id)"
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
          @click.native.stop="editRecord(price.id)"
        />
      </div>
    </div>

    <q-dialog
      v-model="showDelete"
      persistent
    >
      <q-card>
        <q-card-section class="row items-center">
          <span class="q-ml-sm">Are you sure you want to delete <strong>"{{price.organizationId}}"</strong>?</span>
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
            @click.native="deleteSelected(price.id)"
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
    name: 'Price',
    mixins: [ showMixin('price') ],
    props: {
      id: {
        type: String,
        default: ''
      }
    },
    computed: {
      ...mapGetters({
        byId: 'prices/byId'
      }),
      price() {
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
        loadById: 'prices/loadById',
        delete: 'prices/delete'
      })
    }
  }
</script>

<style lang="scss" scoped>
</style>
