const { defineConfig } = require('cypress');

module.exports = defineConfig({
  video: false,
  fixturesFolder: 'cypress/fixtures',
  screenshotsFolder: 'test/screenshots',
  videosFolder: 'test/videos',
  viewportHeight: 900,
  viewportWidth: 1440,
  blockHosts: [],
  e2e: {
    specPattern: '**/*.cy.*',
    supportFile: false,
    baseUrl: 'http://localhost:8081'
  },
});
