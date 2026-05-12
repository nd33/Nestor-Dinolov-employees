import React, { useState } from 'react'
import axios from 'axios'
import { Container, Alert, Spinner } from 'react-bootstrap'
import FileUploader from './FileUploader'
import ResultsDataGrid from './ResultsDataGrid'
import './App.css'

function App() {
  const [selectedFile, setSelectedFile] = useState(null)
  const [loading, setLoading] = useState(false)
  const [result, setResult] = useState(null)
  const [error, setError] = useState(null)

  const handleFileSelect = (event) => {
    const file = event.target.files[0]
    setSelectedFile(file)
    setResult(null)
    setError(null)
  }

  const handleAnalyze = async () => {
    if (!selectedFile) return

    setLoading(true)
    setError(null)
    
    const formData = new FormData()
    formData.append('file', selectedFile)

    try {
      const response = await axios.post('http://localhost:8080/api/employees/analyze', formData, {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      })
      setResult(response.data)
    } catch (err) {
      setError(err.response?.data || 'Error analyzing file')
      console.error('Upload error:', err)
    } finally {
      setLoading(false)
    }
  }

 return (
    <div className="container mt-5">
      <div className="row justify-content-center">
        <div className="col-md-8">
          <h1 className="text-center mb-4">👥 Employee Pair Analyzer</h1>
          <p className="text-center mb-4 text-muted">
            Upload a CSV file to find which two employees have worked together the longest
          </p>
          
          <div className="card shadow-sm">
            <div className="card-body">
              <FileUploader 
                onFileSelect={handleFileSelect}
                onAnalyze={handleAnalyze}
                selectedFile={selectedFile}
                loading={loading}
              />
            </div>
          </div>
          
          {loading && (
            <div className="text-center mt-4">
              <div className="spinner-border text-primary" role="status">
                <span className="visually-hidden">Loading...</span>
              </div>
              <p className="mt-2">Processing your CSV file...</p>
            </div>
          )}
          
          {error && (
            <div className="alert alert-danger mt-3" role="alert">
              <strong>Error:</strong> {error}
            </div>
          )}
          
          {result && <ResultsDataGrid result={result} />}
        </div>
      </div>
    </div>
  )
}

export default App