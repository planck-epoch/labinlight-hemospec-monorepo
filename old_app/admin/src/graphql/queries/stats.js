import gql from 'graphql-tag'


export const GET_STATS_USERS_QUERY = gql`
  query statsUsers($days: Int) {
    statsUsers(days: $days)
  }

  query statsAnalyses($days: Int) {
    statsAnalyses(days: $days)
  }
`
