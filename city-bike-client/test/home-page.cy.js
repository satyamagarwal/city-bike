describe('home page', () => {
  it('shows data unavailable when api server is down', () => {
    cy.intercept('GET', '/station-status', {
        statusCode: 500,
        body: {
          description: 'Internl Server Error',
          errors: ['Server sent either empty or invalid data'],
          code: null
        }
      }
    )

    cy.visit('/')

    cy.contains('Data er ikke tilgjengelig!')
  })

  it('shows list of stations with statuses', () => {
    cy.intercept('GET', '/station-status', (req) => {
        req.reply({
          fixture: 'api-response.json'
        })
      }
    )

    cy.visit('/')
    cy.get('[data-testid="table-tab"]').click()

    cy.get('[data-testid="station-name"]').contains('Adamstuen')
    cy.get('[data-testid="current-index"]').contains('1')

    cy.get('[data-testid="index-4"]').click()
    cy.get('[data-testid="current-index"]').contains('4')
  })

  it('shows result searched from input field', () => {
    cy.intercept('GET', '/station-status', (req) => {
        req.reply({
          fixture: 'api-response.json'
        })
      }
    )

    cy.visit('/')
    cy.get('[data-testid="table-tab"]').click()

    cy.get('[data-testid="search-address"]').type('bjørvika')
    cy.get('[data-testid="station-name"]').contains('Bjørvika')
    cy.get('[data-testid="current-index"]').contains('1')

    cy.get('[data-testid="index-2"]').should('not.exist')
  })
})
