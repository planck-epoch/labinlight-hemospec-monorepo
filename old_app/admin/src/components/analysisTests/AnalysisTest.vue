<template>
  <q-page
    v-if="analysisTest"
    class="q-pa-md"
  >
    <q-list
      bordered
      padding
    >

  
      <q-item v-show="analysisTest.name">
      <q-item-section>
        <q-item-label overline>Name</q-item-label>
        <q-item-label class="q-pa-md">{{analysisTest.name}}</q-item-label>
      </q-item-section>
    </q-item>
      <q-separator spaced />
  
    <q-item v-show="analysisTest.code">
      <q-item-section>
        <q-item-label overline>Code</q-item-label>
        <q-item-label class="q-pa-md">{{analysisTest.code}}</q-item-label>
      </q-item-section>
    </q-item>
    
    <q-item>
      <q-item-section>
        <q-item-label overline>Test Type</q-item-label>
        <q-item-label class="q-pa-md">{{analysisTest.testType}}</q-item-label>
      </q-item-section>
    </q-item>

    <q-item v-show="analysisTest.unit && analysisTest.testType === 'numeric'">
      <q-item-section>
        <q-item-label overline>Unit</q-item-label>
        <q-item-label class="q-pa-md">{{analysisTest.unit}}</q-item-label>
      </q-item-section>
    </q-item>

    <q-item v-show="analysisTest.referenceMale && analysisTest.testType === 'numeric'">
      <q-item-section>
        <q-item-label overline>Reference Male</q-item-label>
        <q-item-label class="q-pa-md">{{analysisTest.referenceMale}}</q-item-label>
      </q-item-section>
    </q-item>

    <q-item v-show="analysisTest.referenceFemale && analysisTest.testType === 'numeric'">
      <q-item-section>
        <q-item-label overline>Reference Female</q-item-label>
        <q-item-label class="q-pa-md">{{analysisTest.referenceFemale}}</q-item-label>
      </q-item-section>
    </q-item>

    <q-item v-show="analysisTest.referenceValue && analysisTest.testType === 'boolean'">
      <q-item-section>
        <q-item-label overline>Reference Value</q-item-label>
        <q-item-label class="q-pa-md">{{analysisTest.referenceValue}}</q-item-label>
      </q-item-section>
    </q-item>

    <q-item v-show="analysisTest.enabled">
      <q-item-section>
        <q-item-label overline>Enabled</q-item-label>
        <q-item-label class="q-pa-md">{{analysisTest.enabled}}</q-item-label>
      </q-item-section>
    </q-item>

      <q-separator spaced />

      <q-item>
        <q-item-section>
          <q-item-label overline>Updated At</q-item-label>
          <q-item-label class="q-pa-md">{{analysisTest.updatedAt}}</q-item-label>
        </q-item-section>
      </q-item>

      <q-separator spaced />

      <q-item>
        <q-item-section>
          <q-item-label overline>Created At</q-item-label>
          <q-item-label class="q-pa-md">{{analysisTest.createdAt}}</q-item-label>
        </q-item-section>
      </q-item>
    </q-list>

    <div class="row q-pa-md">
      <div class="col">
        <q-btn
          class="gt-xs"
          color="negative"
          size="sm"
          @click.native.stop="confirmDelete(analysisTest.id)"
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
          @click.native.stop="editRecord(analysisTest.id)"
        />
      </div>
    </div>

    <q-dialog
      v-model="showDelete"
      persistent
    >
      <q-card>
        <q-card-section class="row items-center">
          <span class="q-ml-sm">Are you sure you want to delete <strong>"{{analysisTest.name}}"</strong>?</span>
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
            @click.native="deleteSelected(analysisTest.id)"
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
    name: 'AnalysisTest',
    mixins: [ showMixin('analysisTest') ],
    props: {
      id: {
        type: String,
        default: ''
      }
    },
    computed: {
      ...mapGetters({
        byId: 'analysisTests/byId'
      }),
      analysisTest() {
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
        loadById: 'analysisTests/loadById',
        delete: 'analysisTests/delete'
      })
    }
  }
</script>

<style lang="scss" scoped>
</style>
