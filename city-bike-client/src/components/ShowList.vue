<template>
  <TableHeading />
  <template v-if="stationsWithPageNumber.length > 0">
    <template
      v-for="station in stationsWithPageNumber[currentPageNumber - 1]"
      :key="station.id"
    >
      <div class="osg-grid osg-grid--gap-row">
        <div class="osg-grid__column--4 osg-color-bg-blue-dark osg-padding-2">
          <div
            class="osg-full-height osg-flex osg-flex-justify-content-center osg-flex-align-items-center osg-color-text-white"
            data-testid="station-name"
          >
            {{ station.info.name }}
          </div>
        </div>
        <div class="osg-grid__column--4 osg-color-bg-blue-light">
          <div
            class="osg-full-height osg-flex osg-flex-justify-content-center osg-flex-align-items-center osg-color-text-black"
          >
            {{ station.info.address }}
          </div>
        </div>
        <div class="osg-grid__column--2 osg-color-bg-blue-dark">
          <div
            class="osg-full-height osg-flex osg-flex-justify-content-center osg-flex-align-items-center osg-color-text-white"
          >
            {{ station.status.numBikesAvailable }}
          </div>
        </div>
        <div class="osg-grid__column--2 osg-color-bg-blue-light">
          <div
            class="osg-full-height osg-flex osg-flex-justify-content-center osg-flex-align-items-center osg-color-text-black"
          >
            {{ station.status.numDocksAvailable }}
          </div>
        </div>
      </div>
    </template>
    <div class="osg-grid__column--4 osg-padding-2">
      <div 
        id="vue-pagination"
        class="osg-full-height osg-flex osg-flex-justify-content-center osg-flex-align-items-center"
      >
        <OsgPagination
          aria-label="Pagination, first page"
          :disable-current-index-btn="false"
          :current-index="currentPageNumber"
          :total-pages="totalPages"
          :limit="10"
          @paginate="(num) => currentPageNumber = num"
        />
      </div>
    </div>
  </template>
  <template v-else>
    <DataUnavailable />
  </template>
</template>

<script>
import OsgPagination from "./external/OsgPagination.vue"
import TableHeading from "./TableHeading.vue";
import DataUnavailable from "./DataUnavailable.vue";

export default {
  name: "ShowList",
  components: {
    DataUnavailable,
    TableHeading,
    OsgPagination
  },
  props: {
    searchParam: {
      type: String,
      default: ''
    },
    stationsAndStatuses: {
      type: Array,
      default() { return [] }
    }
  },
  data() {
    return {
      pageNumber: 1
    }
  },
  computed: {
    stationsWithPageNumber() {
      const list = this.stationsAndStatuses

      return list
          .reduce(
              (result, currentValue) => {
                if (currentValue) {
                  const pageNo = Math.floor(list.indexOf(currentValue) / 18)

                  if (result[pageNo]) {
                    result[pageNo].includes(currentValue) ? [] : result[pageNo].push(currentValue)
                  } else {
                    result[pageNo] = [currentValue]
                  }
                }

                return result
              },
              []
          )
    },
    totalPages() {
      const pages = this.stationsWithPageNumber.length

      return pages > 0 ? pages : 1
    },
    currentPageNumber: {
      get() {
        return this.pageNumber
      },
      set(newPageNumber) {
        this.totalPages >= newPageNumber ? this.pageNumber = newPageNumber : this.pageNumber = 1
      }
    },
  },
  watch: {
    searchParam() {
      this.currentPageNumber = 1
    }
  }
}
</script>
