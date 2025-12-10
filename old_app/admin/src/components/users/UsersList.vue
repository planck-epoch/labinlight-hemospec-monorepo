<template>
  <q-page class="q-pa-md">
    <q-list
      bordered
      class="rounded-borders"
    >
      <div class="row items-center crud-header">
        <div class="col-xs-12 col-sm-5">
          <q-item-label header class="row items-center">
            <div class="col">
              Users
            </div>

            <div class="col-1">
              <q-btn
                flat
                dense
                round
                aria-label="Filter"
                icon="filter_alt"
                class="xs align-right"
                @click="filterOpen = !filterOpen"
              />
            </div>
          </q-item-label>
        </div>
        <div class="col-xs-12 col-sm-7 filter-col" :class="{'filter-open': filterOpen}">
          <q-item-label
            header
            class="filter-holder"
          >
            <q-input
              v-model="searchValue"
              label="Search"
              @keyup.enter.native="onSearch"
              @clear="onSearch"
              clearable
              dense
            >
              <template v-slot:append>
                <q-icon name="search" />
              </template>
            </q-input>
          </q-item-label>
        </div>
      </div>

      <q-item>
        <q-item-section avatar class="col-xs-2 col-sm-1">
          <q-checkbox v-model="allChecked" />
        </q-item-section>

        <q-item-section class="col-xs-7">
          <q-item-label overline>
            Email
          </q-item-label>
        </q-item-section>

        <q-item-section class="col-xs-3 col-sm-2">
          <q-item-label overline>
            Created At
          </q-item-label>
        </q-item-section>

        <q-item-section class="col-xs-0 col-sm-2">
        </q-item-section>
      </q-item>

      <q-item
        v-for="(user, index) in users"
        :key="user.id"
        v-ripple
        clickable
        class="crud-item"
        :to="`/users/${user.id}`"
      >
        <q-item-section avatar class="col-xs-2 col-sm-1">
          <q-checkbox v-model="selected[index]" />
        </q-item-section>

        <q-item-section class="col-xs-7">
          <q-item-label>{{ user.email }}</q-item-label>
        </q-item-section>

        <q-item-section class="col-xs-3 col-sm-2">
          <q-item-label class="text-grey-8">
            {{ $formatFullDate(user.createdAt) }}
          </q-item-label>
        </q-item-section>

        <q-item-section side class="col-xs-0 col-sm-2">
          <div class="text-grey-8 q-gutter-xs">
            <q-btn
              class="gt-xs"
              size="12px"
              flat
              dense
              round
              icon="delete"
              @click.native.prevent.stop="confirmDelete(user.id)"
            />
            <q-btn
              class="gt-xs"
              size="12px"
              flat
              dense
              round
              icon="edit"
              :to="`/users/${user.id}/edit`"
            />
          </div>
        </q-item-section>
      </q-item>

      <div v-if="multiActionable"
        class="q-pt-lg q-pl-lg full-width row wrap items-center content-center">
        <div class="col-xs-12">
          <q-btn
            size="sm"
            color="negative"
            @click.native.stop="confirmDelete()"
          >
            Delete ({{selectedIds.length}})
          </q-btn>
        </div>
      </div>

      <div class="q-pa-lg full-width row wrap justify-between items-center content-center">
        <div class="col-xs-12 col-sm-3 text-center text-sm-left q-py-sm">
          Total: {{metadata.totalCount}}
        </div>

        <div class="col-xs-12 col-sm-6 flex flex-center q-py-sm">
          <q-pagination
            v-model="currentPage"
            :max="metadata.totalPages"
            :direction-links="true"
            :input="true"
          >
          </q-pagination>
        </div>

        <div class="col-xs-12 col-sm-3 text-center text-sm-right q-py-sm">
          <q-btn
            size="sm"
            class="gt-xs float-right"
            color="primary"
            label="New"
            @click.native.stop="newRecord()"
          />
        </div>
      </div>
    </q-list>

    <q-dialog
      v-model="showDelete"
      persistent
    >
      <q-card>
        <q-card-section class="row items-center">
          <span class="q-ml-sm">Are you sure you want to delete <strong>"{{ deletedScopes }}"</strong>?</span>
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
            @click.native="deleteSelected()"
          />
        </q-card-actions>
      </q-card>
    </q-dialog>
  </q-page>
</template>

<script>
  import { mapGetters } from 'vuex'
  import { paginationMixin, showMixin } from '@/mixins/crud/pagination'

  export default {
    name: 'UsersList',
    mixins: [ paginationMixin('user', 'email'), showMixin('user') ],
    computed: {
      ...mapGetters({
        users: 'users/all'
      }),
    },
    mounted() {
      if (this.$route.query.search) {
        this.searchValue = this.$route.query.search
        Object.assign(this.filtering, {search: this.searchValue})
      }

      this.load({page: 1})
    }
  }
</script>

<style lang="scss" scoped>
</style>
