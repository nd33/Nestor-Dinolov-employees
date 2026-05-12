import React from 'react'
import { Button, Form } from 'react-bootstrap'

function FileUploader({ onFileSelect, onAnalyze, selectedFile, loading }) {
  return (
    <div className="mb-4">
      <Form.Group controlId="formFile" className="mb-3">
        <Form.Label>Select CSV File</Form.Label>
        <Form.Control
          type="file"
          accept=".csv"
          onChange={onFileSelect}
          disabled={loading}
        />
        <Form.Text className="text-muted">
          Upload CSV with columns: EmpID, ProjectID, DateFrom, DateTo
        </Form.Text>
      </Form.Group>
      
      <Button 
        variant="primary" 
        onClick={onAnalyze}
        disabled={!selectedFile || loading}
      >
        {loading ? 'Analyzing...' : 'Analyze Employee Pairs'}
      </Button>
    </div>
  )
}

export default FileUploader