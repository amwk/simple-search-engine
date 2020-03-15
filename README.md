# Usage

Basic functionality is provided via REST endpoints. Some sample requests can be found under ./request directory

addDocuments.http - this file contain REST request for adding new documents
getAllDocuments.http - retrieve all added documents - handy for verification which documents are present
search.http - this file contain requests for searching following terms: "brown" and "fox"

There is also possibility to add/ update or remove single document

Project utilize Swagger for API documentation - after spinning up Spring Boot it can be found under url : http://localhost:8080/swagger-ui.html


# Example
The following documents are indexed:
Document 1: “the brown fox jumped over the brown dog”   
Document 2: “the lazy brown dog sat in the corner”  
Document 3: “the red fox bit the lazy dog”  
~~A search for “brown” should now return the list: [document 1, document 2].~~ 
~~A search for “fox” should return the list: [document 1, document 3].~~  

According to TF-IDF:
A search for “brown” should now return the list: [document 1, document 2].
A search for “fox” should return the list: [document 3, document 1].

