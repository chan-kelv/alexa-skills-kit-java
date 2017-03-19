package main

import (
	"database/sql"
	"encoding/json"
    "fmt"
    "html"
    "io/ioutil"
    "log"
    "math/rand"
    "net/http"
    "os"
    "strings"
    "time"

    "github.com/gorilla/mux"
    _ "github.com/lib/pq"
)

var (
	TriviaList []QuizletQuestion
)

type QuizletQuestion struct {
	Question string `json:"term"`
	Answer string `json:"definition"`
	QuestionIdx int `json:"rank"`
}

type QuizletReply struct {
	Questions QuizletQuestions `json:"terms"`
}

type QuizletQuestions []QuizletQuestion

func main() {
	InitializeQuestions()
	InitializeDatabase()
	fmt.Println("Question list:", TriviaList)

    router := mux.NewRouter().StrictSlash(true)
    router.HandleFunc("/", Index)
    router.HandleFunc("/random", RandomizeQuestions)
    log.Fatal(http.ListenAndServe(":8081", router))
}

func InitializeQuestions() {
	s := "https://api.quizlet.com/2.0/sets/118552361?client_id=ypc6Ext7PE&whitespace=1"
	resp, err := http.Get(s)
	if err != nil {
		fmt.Println("Malformed get", err)
		os.Exit(1)
	}
	bytes, _ := ioutil.ReadAll(resp.Body)

	// Find index of terms
	idx := strings.Index(string(bytes), "\"terms\":")
	m := new(QuizletReply)
	if idx > 0 {
		substring := string(bytes)[idx:len(string(bytes))]
		substring = "{" + substring
		//fmt.Println(substring)

		err = json.Unmarshal([]byte(substring), &m)
		if err != nil {
			fmt.Println("Can't unmarshal", err)
			os.Exit(1)
		}

		//fmt.Println(m)
	}

	for _, q := range m.Questions {
		TriviaList = append(TriviaList, q)
	}
}

/*
Database name: questions
Admin user: maxroach 
*/

func InitializeDatabase() {
	db, err := sql.Open("postgres", "postgresql://maxroach@localhost:26257/questions?sslmode=disable")
	if err != nil {
		log.Fatalf("error connection to the database: %s", err)
	}
	defer db.Close()

	// Create the "questions" table.
	if _, err := db.Exec(
		"CREATE TABLE IF NOT EXISTS Questions (ID SERIAL PRIMARY KEY, Question varchar(1024), Answer varchar(1024), Type varchar(32))"); err != nil {
		log.Fatal(err)
	}

	// Insert all of the questions and all of the answers
	for _, q := range TriviaList {
		if (strings.Index(q.Question, "'") == -1) && (strings.Index(q.Answer, "'") == -1) {
			s := fmt.Sprintf("INSERT INTO Questions (Question, Answer, Type) VALUES ('%s', '%s', 'Trivia')", 
				q.Question, q.Answer)
			if _, err := db.Exec(s); err != nil {
				log.Fatal(err)
			}
		}
	}

	rows, err := db.Query("SELECT Question FROM Questions")
	if err != nil {
		log.Fatal(err)
	}
	defer rows.Close()

	for rows.Next() {
		var question string
		if err := rows.Scan(&question); err != nil {
			log.Fatal(err)
		}
		fmt.Println("Question:", question)
	}
}

func Index(w http.ResponseWriter, r *http.Request) {
    fmt.Fprintf(w, "Hello world, %q", html.EscapeString(r.URL.Path))
}

func RandomizeQuestions(w http.ResponseWriter, r *http.Request) {
	rand.Seed(time.Now().Unix())

	json.NewEncoder(w).Encode(TriviaList[rand.Intn(len(TriviaList))])
}