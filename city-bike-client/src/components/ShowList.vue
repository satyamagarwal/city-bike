<template>
  <div class="osg-grid osg-grid--gap-row osg-margin-bottom-2">
    <div class="osg-grid__column--4 osg-color-bg-blue-dark">
      <div class="osg-full-height osg-flex osg-flex-justify-content-center osg-flex-align-items-center osg-color-text-white osg-text-size-india">
        Stasjons navn
      </div>
    </div>
    <div class="osg-grid__column--4 osg-color-bg-blue-light">
      <div class="osg-full-height osg-flex osg-flex-justify-content-center osg-flex-align-items-center osg-color-text-black osg-text-size-india">
        Adress
      </div>
    </div>
    <div class="osg-grid__column--2 osg-color-bg-blue-dark">
      <div class="osg-full-height osg-flex osg-flex-justify-content-center osg-flex-align-items-center osg-color-text-white osg-text-size-india">
        Tilgjenglige sykkler
      </div>
    </div>
    <div class="osg-grid__column--2 osg-color-bg-blue-light">
      <div class="osg-full-height osg-flex osg-flex-justify-content-center osg-flex-align-items-center osg-color-text-black osg-text-size-india">
        Tilgjenglige låser
      </div>
    </div>
  </div>
  <template v-if="stationsWithPageNumber.length > 0">
    <template v-for="station in stationsWithPageNumber[currentPageNumber - 1]" :key="station.id">
      <div class="osg-grid osg-grid--gap-row">
        <div class="osg-grid__column--4 osg-color-bg-blue-dark osg-padding-2">
          <div class="osg-full-height osg-flex osg-flex-justify-content-center osg-flex-align-items-center osg-color-text-white" data-testid="station-name">
            {{ station.info.name }}
          </div>
        </div>
        <div class="osg-grid__column--4 osg-color-bg-blue-light">
          <div class="osg-full-height osg-flex osg-flex-justify-content-center osg-flex-align-items-center osg-color-text-black">
            {{ station.info.address }}
          </div>
        </div>
        <div class="osg-grid__column--2 osg-color-bg-blue-dark">
          <div class="osg-full-height osg-flex osg-flex-justify-content-center osg-flex-align-items-center osg-color-text-white">
            {{ station.status.numBikesAvailable }}
          </div>
        </div>
        <div class="osg-grid__column--2 osg-color-bg-blue-light">
          <div class="osg-full-height osg-flex osg-flex-justify-content-center osg-flex-align-items-center osg-color-text-black">
            {{ station.status.numDocksAvailable }}
          </div>
        </div>
      </div>
    </template>
    <div class="osg-grid__column--4 osg-padding-2">
      <div id="vue-pagination" class="osg-full-height osg-flex osg-flex-justify-content-center osg-flex-align-items-center">
        <Pagination aria-label="Pagination, first page" :disable-current-index-btn="false" :current-index="currentPageNumber"
                    :total-pages="totalPages" v-on:paginate="(num) => currentPageNumber = num" :limit="10">
        </Pagination>
      </div>
    </div>
  </template>
  <template v-else>
    <div class="osg-grid osg-grid--gap-row">
      <div class="osg-grid__column--12 osg-padding-2">
        <div class="osg-full-height osg-flex osg-flex-justify-content-center osg-flex-align-items-center osg-color-text-red">
          Data er ikke tilgjengelig!
        </div>
      </div>
    </div>
  </template>
</template>

<script>
import Pagination from "./pagination.vue"

export default {
  name: "ShowList",
  props: {
    searchParam: String,
    stationsAndStatuses: Array
  },
  components: {
    Pagination
  },
  data() {
    return {
      search: '',
      pageNumber: 1
    }
  },
  computed: {
    stationsWithPageNumber() {
      const list = this.filteredStations()

      return list
          .reduce(
              (result, currentValue) => {
                if (currentValue) {
                  const pageNo = Math.floor(list.indexOf(currentValue) / 20)

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
    }
  },
  watch: {
    searchParam() {
      this.currentPageNumber = 1
    }
  },
  methods: {
    filteredStations() {
      const lowerCasedSearch = this.searchParam.toLocaleLowerCase()
      return this
          .stationsAndStatuses
          .filter((stationAndStatus) =>
              stationAndStatus.info.name.toLocaleLowerCase().includes(lowerCasedSearch) ||
              stationAndStatus.info.address.includes(lowerCasedSearch)
          )
          .sort((station1, station2) => station1.info.name.localeCompare(station2.info.name))
    }
  }
}
</script>
