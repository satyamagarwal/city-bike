<template>
  <div class="osg-grid osg-grid--gap-row osg-margin-top-4 osg-margin-bottom-4">
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
  <template v-if="filteredStations.length > 0">
    <template v-for="stationAndStatus in filteredStations" :key="stationAndStatus.id">
      <div class="osg-grid osg-grid--gap-row">
        <div class="osg-grid__column--4 osg-color-bg-blue-dark osg-padding-2">
          <div class="osg-full-height osg-flex osg-flex-justify-content-center osg-flex-align-items-center osg-color-text-white">
            {{ stationAndStatus.info.name }}
          </div>
        </div>
        <div class="osg-grid__column--4 osg-color-bg-blue-light">
          <div class="osg-full-height osg-flex osg-flex-justify-content-center osg-flex-align-items-center osg-color-text-black">
            {{ stationAndStatus.info.address }}
          </div>
        </div>
        <div class="osg-grid__column--2 osg-color-bg-blue-dark">
          <div class="osg-full-height osg-flex osg-flex-justify-content-center osg-flex-align-items-center osg-color-text-white">
            {{ stationAndStatus.status.numBikesAvailable }}
          </div>
        </div>
        <div class="osg-grid__column--2 osg-color-bg-blue-light">
          <div class="osg-full-height osg-flex osg-flex-justify-content-center osg-flex-align-items-center osg-color-text-black">
            {{ stationAndStatus.status.numDocksAvailable }}
          </div>
        </div>
      </div>
    </template>
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
export default {
  name: "ShowList",
  props: {
    searchParam: String,
    stationsAndStatuses: Array
  },
  data () {
    return {
      search: ''
    }
  },
  computed: {
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
