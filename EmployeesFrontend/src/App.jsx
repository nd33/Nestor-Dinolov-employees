import React, { useState } from 'react'
import axios from 'axios'
import { Container, Alert, Spinner } from 'react-bootstrap'
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
    <Container className="mt-5">
      <h1 className="text-center mb-4">Employee Pair Analyzer</h1>
      <p className="text-center mb-4">
        Find which two employees have worked together the longest
      </p>
      
      <FileUploader 
        onFileSelect={handleFileSelect}
        onAnalyze={handleAnalyze}
        selectedFile={selectedFile}
        loading={loading}
      />
      
      {loading && (
        <div className="text-center mt-4">
          <Spinner animation="border" role="status">
            <span className="visually-hidden">Loading...</span>
          </Spinner>
        </div>
      )}
      
      {error && (
        <Alert variant="danger" className="mt-3">
          {error}
        </Alert>
      )}
      
      {result && (
        <Alert variant="success" className="mt-3">
          <h4>Result:</h4>
          <p>Employees {result.empId1} and {result.empId2} worked together for {result.totalDaysWorked} days</p>
        </Alert>
      )}
    </Container>
  )
}

export default App