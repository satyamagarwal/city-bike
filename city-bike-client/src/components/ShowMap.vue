<template>
  <div
    id="osg-map2"
    class="osg-map"
    aria-hidden="true"
  >
    <OsgMap
      v-if="stationsAndStatuses.length > 0"
      ref="osgMap2"
      api-key="VW1Tn0fx1t3b6t0CHP6Q"
      :state="{ longitude: 10.74981, latitude: 59.913008, zoom: 10, autoFitToBounds: true, showPopups: true }"
      :geo-json="geoJson"
      :clustered-points="true"
      :ratio="'osg-ratio-16-9'"
    />
    <template v-else>
      <DataUnavailable />
    </template>
  </div>
</template>

<script>
import DataUnavailable from "./DataUnavailable.vue";
import OsgMap from "styleguide/src/components/map/map.vue";

export default {
  name: "ShowMap",
  components: {
    DataUnavailable,
    OsgMap
  },
  props: {
    stationsAndStatuses: {
      type: Array,
      default() { return [] }
    }
  },
  computed: {
    geoJson() {
      const bikeFeatures = this
          .stationsAndStatuses
          .map((station) => {
            const {id, info, status} = station

            return {
              type: "Feature",
              geometry: {
                type: "Point",
                coordinates: [info.location.long, info.location.lat]
              },
              properties: {
                popupEvent: "ioFavouriteMapOpen",
                popupEventData: {
                  id: id
                },
                popdownEvent: "ioFavouriteMapClose",
                popupContent: `
                  <h2 class="osg-text-size-hotel">${info.name}</h2>
                  <strong>Address: </strong><span>${info.address}</span>
                  <br>
                  <strong>Tilgjenglige sykkler: </strong><span>${status.numBikesAvailable}</span>
                  <br>
                  <strong>Tilgjenglige låser: </strong><span>${status.numBikesAvailable}</span>
                  <br>
                  `
              }
            }
          })

      return {
        type: "FeatureCollection",
        features: bikeFeatures
      }
    }
  }
}
</script>
