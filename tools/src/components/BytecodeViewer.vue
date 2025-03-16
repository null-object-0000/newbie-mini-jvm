<template>
  <div>
    <h1>Java字节码查看器</h1>
    <div class="upload-container" 
         @dragover.prevent="handleDragOver"
         @dragleave.prevent="handleDragLeave"
         @drop.prevent="handleDrop"
         :class="{ 'drag-over': isDragging }">
      <label for="fileInput" class="upload-label">
        选择.class文件或拖拽文件到此处
      </label>
      <input type="file" id="fileInput" accept=".class" @change="handleFileChange">
    </div>
    <div id="hexDisplay" class="hex-display">
      <div v-for="(row, index) in hexRows" :key="index" class="hex-row">
        <span class="offset">{{ row.offset }}</span>
        <span class="hex-content">{{ row.hexContent }}</span>
        <span class="ascii-content">{{ row.asciiContent }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';

const hexRows = ref([]);
const BYTES_PER_ROW = 16;
const isDragging = ref(false);

const processFile = (file) => {
  if (!file || !file.name.endsWith('.class')) {
    alert('请选择.class文件');
    return;
  }

  const reader = new FileReader();
  reader.onload = (e) => {
    const buffer = e.target.result;
    const bytes = new Uint8Array(buffer);
    displayHexDump(bytes);
  };
  reader.readAsArrayBuffer(file);
};

const handleFileChange = (e) => {
  const file = e.target.files[0];
  processFile(file);
};

const handleDragOver = () => {
  isDragging.value = true;
};

const handleDragLeave = () => {
  isDragging.value = false;
};

const handleDrop = (e) => {
  isDragging.value = false;
  const file = e.dataTransfer.files[0];
  processFile(file);
};

const displayHexDump = (bytes) => {
  const rows = [];

  for (let i = 0; i < bytes.length; i += BYTES_PER_ROW) {
    const offset = i.toString(16).padStart(8, '0');
    let hexContent = '';
    let asciiContent = '';

    for (let j = 0; j < BYTES_PER_ROW; j++) {
      if (i + j < bytes.length) {
        const byte = bytes[i + j];
        hexContent += byte.toString(16).padStart(2, '0') + ' ';
        asciiContent += (byte >= 32 && byte <= 126) ? String.fromCharCode(byte) : '.';
      } else {
        hexContent += '   ';
        asciiContent += ' ';
      }
    }

    rows.push({
      offset,
      hexContent,
      asciiContent
    });
  }

  hexRows.value = rows;
};
</script>

<style scoped>
body {
  font-family: Arial, sans-serif;
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  background-color: #f5f5f5;
}

.upload-container {
  text-align: center;
  margin-bottom: 20px;
  padding: 20px;
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  border: 2px dashed #ccc;
  transition: all 0.3s ease;
}

.upload-container.drag-over {
  border-color: #4CAF50;
  background-color: #f0f9f0;
}

.hex-display {
  font-family: monospace;
  white-space: pre;
  background-color: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  overflow-x: auto;
}

.hex-row {
  display: flex;
  margin-bottom: 4px;
}

.offset {
  color: #666;
  width: 100px;
}

.hex-content {
  margin-right: 20px;
}

.ascii-content {
  color: #666;
}

.upload-label {
  display: inline-block;
  padding: 10px 20px;
  background-color: #4CAF50;
  color: white;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.upload-label:hover {
  background-color: #45a049;
}

#fileInput {
  display: none;
}

h1 {
  color: #333;
  text-align: center;
  margin-bottom: 30px;
}
</style>