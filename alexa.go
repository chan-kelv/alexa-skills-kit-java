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
	MasterQuestionList []QuizletQuestion
	DB *sql.DB
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
	/*
	Database name: questions
	Admin user: maxroach 
	*/
	db, err := sql.Open("postgres", "postgresql://maxroach@localhost:26257/questions?sslmode=disable")
	if err != nil {
		log.Fatalf("error connection to the database: %s", err)
	}
	DB = db
	defer db.Close()

	InitializeQuestions(db)

    router := mux.NewRouter().StrictSlash(true)
    router.HandleFunc("/", Index)
    router.HandleFunc("/categories", GetCategories)
    router.HandleFunc("/random", RandomizeQuestions)
    log.Fatal(http.ListenAndServe(":8081", router))
}

func InitializeQuestions(db *sql.DB) {
	GetTriviaQuestions(db)
	//GetSpaceQuestions(db)
}

func GetTriviaQuestions(db *sql.DB) {
	/*
	rows, err := db.Query("SELECT Question FROM Questions WHERE Type = 'Trivia'")
	if err != nil {
		log.Fatal(err)
	}
	defer rows.Close()

	for rows.Next() {
		// there are rows, don't bother requesting the questions again
		return
	}
	*/

	fmt.Println("Reparsing Trivia")
	// Database empty: parse questions
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
	}

	// Drop previously existing "questions" table.
	if _, err := db.Exec(
		"DROP TABLE Questions"); err != nil {
		log.Fatal(err)
	}

	// Create the "questions" table.
	if _, err := db.Exec(
		"CREATE TABLE IF NOT EXISTS Questions (ID SERIAL PRIMARY KEY, Question varchar(1024), Answer varchar(1024), Type varchar(32))"); err != nil {
		log.Fatal(err)
	}

	fmt.Println("Questions len", len(m.Questions))
	// Insert all of the questions and all of the answers
	for _, q := range m.Questions {
		// Take questions and answers without aposterphes
		// and questions with answers 2 words or less
		// and questions with more than 2 words
		if (strings.Index(q.Question, "'") == -1) && (strings.Index(q.Answer, "'") == -1) && 
		(len(strings.Fields(q.Answer)) < 3) && (len(strings.Fields(q.Question)) > 2) {
			s := fmt.Sprintf("INSERT INTO Questions (Question, Answer, Type) VALUES ('%s', '%s', 'Trivia')", 
				q.Question, q.Answer)
			MasterQuestionList = append(MasterQuestionList, q)
			if _, err := db.Exec(s); err != nil {
				log.Fatal(err)
			}
		}
	}
}

func GetSpaceQuestions(db *sql.DB) {
	/*
	rows, err := db.Query("SELECT Question FROM Questions")
	if err != nil {
		log.Fatal(err)
	}
	defer rows.Close()

	for rows.Next() {
		// there are rows, don't bother requesting the questions again
		return
	}
	*/

	// Database empty: parse Space
	s := "https://api.quizlet.com/2.0/sets/119475430?client_id=ypc6Ext7PE&whitespace=1"
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
	}

	
	// Drop previously existing "space" table.
	if _, err := db.Exec(
		"DROP TABLE Space"); err != nil {
		//log.Fatal(err)
	}

	// Create the "questions" table.
	if _, err := db.Exec(
		"CREATE TABLE IF NOT EXISTS Space (ID SERIAL PRIMARY KEY, Question varchar(1024), Answer varchar(1024), Type varchar(32))"); err != nil {
		log.Fatal(err)
	}

	// Insert all of the questions and all of the answers
	fmt.Println("Space Questions len", len(m.Questions))
	for _, q := range m.Questions {
		// Take questions and answers without aposterphes
		if (strings.Index(q.Question, "'") == -1) && (strings.Index(q.Answer, "'") == -1) {
			s := fmt.Sprintf("INSERT INTO Space (Question, Answer, Type) VALUES ('%s', '%s', 'Space')", 
				q.Answer, q.Question)
			MasterQuestionList = append(MasterQuestionList, q)
			if _, err := db.Exec(s); err != nil {
				log.Fatal(err)
			}
		}
	}
}


func Index(w http.ResponseWriter, r *http.Request) {
    fmt.Fprintf(w, "Hello world, %q", html.EscapeString(r.URL.Path))
}

func GetCategories(w http.ResponseWriter, r *http.Request) {
	rows, err := DB.Query("SELECT DISTINCT q.Type, s.Type FROM questions AS q, Space AS s")
	if err != nil {
		log.Fatal("Error parsing categories")
	}
	defer rows.Close()

	categories := make([]string, 0)
	for rows.Next() {
		// TODO: this is hacky -- every time you add a table, this needs a new entry
		var trivia string
		var space string
		err = rows.Scan(&trivia, &space)

		categories = append(categories, trivia)
		categories = append(categories, space)
	}

	json.NewEncoder(w).Encode(categories)
}

func RandomizeQuestions(w http.ResponseWriter, r *http.Request) {
	//rows, err := DB.Query("SELECT * FROM Questions, Space")

	rand.Seed(time.Now().Unix())
	json.NewEncoder(w).Encode(MasterQuestionList[rand.Intn(len(MasterQuestionList))])
}