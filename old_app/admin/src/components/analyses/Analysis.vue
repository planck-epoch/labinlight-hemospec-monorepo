<template>
  <q-page
    v-if="analysis"
    class="q-pa-md"
  >
    <q-list
      bordered
      padding
    >
  
      <q-item v-show="analysis.organizationId">
        <q-item-section>
          <q-item-label overline>Organization</q-item-label>
          <q-item-label class="q-pa-md">{{analysis.organization.name}}</q-item-label>
        </q-item-section>
      </q-item>
      <q-separator spaced />
    
      <q-item v-show="analysis.analysisBundleId">
        <q-item-section>
          <q-item-label overline>Analysis Type</q-item-label>
          <q-item-label class="q-pa-md">{{analysis.analysisBundle.name}}</q-item-label>
        </q-item-section>
      </q-item>
      <q-separator spaced />
    
      <q-item v-show="analysis.processNumber">
        <q-item-section>
          <q-item-label overline>Process Number</q-item-label>
          <q-item-label class="q-pa-md">{{analysis.processNumber}}</q-item-label>
        </q-item-section>
      </q-item>
      <q-separator spaced />
    
      <q-item v-show="analysis.sexCode">
        <q-item-section>
          <q-item-label overline>Sex</q-item-label>
          <q-item-label class="q-pa-md">{{analysis.sexCode}}</q-item-label>
        </q-item-section>
      </q-item>
      <q-separator spaced />
    
      <q-item v-show="analysis.birthYear">
        <q-item-section>
          <q-item-label overline>Birth Year</q-item-label>
          <q-item-label class="q-pa-md">{{analysis.birthYear}}</q-item-label>
        </q-item-section>
      </q-item>
      <q-separator spaced />
    
      <q-item v-show="analysis.countryCode">
        <q-item-section>
          <q-item-label overline>Country</q-item-label>
          <q-item-label class="q-pa-md">{{analysis.countryCode}}</q-item-label>
        </q-item-section>
      </q-item>
      <q-separator spaced />
    
      <q-item v-show="analysis.healthNumber">
        <q-item-section>
          <q-item-label overline>Health Number</q-item-label>
          <q-item-label class="q-pa-md">{{analysis.healthNumber}}</q-item-label>
        </q-item-section>
      </q-item>
      <q-separator spaced />
    
      <q-item v-show="analysis.phone">
        <q-item-section>
          <q-item-label overline>Phone</q-item-label>
          <q-item-label class="q-pa-md">{{analysis.phone}}</q-item-label>
        </q-item-section>
      </q-item>
      <q-separator spaced />
    
      <q-item v-show="analysis.payload">
        <q-item-section>
          <q-item-label overline>Payload</q-item-label>
          <v-jsoneditor v-model="analysis.payload" :options="{mode: 'view'}" height="500px" />
        </q-item-section>
      </q-item>

      <q-item v-show="analysis.results">
        <q-item-section>
          <q-item-label overline>Results</q-item-label>
          <v-jsoneditor v-model="analysis.results" :options="{mode: 'view'}" height="250px" />
        </q-item-section>
      </q-item>

      <q-separator spaced />

      <q-item>
        <q-item-section>
          <q-item-label overline>Updated At</q-item-label>
          <q-item-label class="q-pa-md">{{analysis.updatedAt}}</q-item-label>
        </q-item-section>
      </q-item>

      <q-separator spaced />

      <q-item>
        <q-item-section>
          <q-item-label overline>Created At</q-item-label>
          <q-item-label class="q-pa-md">{{analysis.createdAt}}</q-item-label>
        </q-item-section>
      </q-item>
    </q-list>

    <div class="row q-pa-md">
      <div class="col">
        <q-btn
          class="gt-xs"
          color="negative"
          size="sm"
          @click.native.stop="confirmDelete(analysis.id)"
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
          @click.native.stop="editRecord(analysis.id)"
        />
      </div>
    </div>

    <q-dialog
      v-model="showDelete"
      persistent
    >
      <q-card>
        <q-card-section class="row items-center">
          <span class="q-ml-sm">Are you sure you want to delete <strong>"{{analysis.organizationId}}"</strong>?</span>
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
            @click.native="deleteSelected(analysis.id)"
          />
        </q-card-actions>
      </q-card>
    </q-dialog>
  </q-page>
</template>

<script>
  import { mapGetters, mapActions } from 'vuex'
  import { showMixin } from '@/mixins/crud/pagination'
  import VJsoneditor from 'v-jsoneditor/src/index'

  export default {
    name: 'Analysis',
    components: {
      VJsoneditor
    },
    mixins: [ showMixin('analysis') ],
    props: {
      id: {
        type: String,
        default: ''
      }
    },
    computed: {
      ...mapGetters({
        byId: 'analyses/byId'
      }),
      analysis() {
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
        loadById: 'analyses/loadById',
        delete: 'analyses/delete'
      })
    }
  }
</script>

<style lang="scss" scoped>
</style>
