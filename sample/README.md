# Sample OpenSearch query for flight "SPAR309"

curl -X GET -H "Content-Type: application/json" -d @search_example.json  "http://localhost:9200/military_flights_geo/_search?pretty"
