from keras.applications import MobileNet
from keras.models import Model
from keras.layers import Dense, GlobalAveragePooling2D
from keras.preprocessing.image import ImageDataGenerator
from keras.optimizers import Adam
from keras.callbacks import ModelCheckpoint, EarlyStopping, ReduceLROnPlateau
import matplotlib.pyplot as plt

num_labels = 7  #number of labels or moods to be detected

#MobileNet is designed to work with images of dim 224,224
img_rows, img_cols = 224, 224

#MobileNet model is defined and initialized
MobileNet = MobileNet(weights='imagenet', include_top=False, input_shape=(img_rows, img_cols, 3))

#Layers are set to trainable as True by default
for layer in MobileNet.layers:
    layer.trainable = False

#All of the layers in MobileNet model are printed along with Boolean value True or False
for (i, layer) in enumerate(MobileNet.layers):
    print(str(i), layer.__class__.__name__, layer.trainable)

#creates the top or head of the model that will be  placed ontop of the bottom layers
def addTopModelMobileNet(bottom_model, num_classes):

    top_model = bottom_model.output
    top_model = GlobalAveragePooling2D()(top_model)
    top_model = Dense(1024, activation='relu')(top_model)
    top_model = Dense(1024, activation='relu')(top_model)
    top_model = Dense(512, activation='relu')(top_model)
    top_model = Dense(num_labels, activation='softmax')(top_model)

    return top_model


#connecting all of the layers with the bottom model
FC_Head = addTopModelMobileNet(MobileNet, num_labels)

#creating the model with MobileNet as the bottom model
model = Model(inputs=MobileNet.input, outputs=FC_Head)

print(model.summary())
#this prints the layers, their types, their output shapes and the number of learnable parameters

#path for training and validation data batches to be stored
train_data_dir = 'path-to-training-data-folder'
validation_data_dir = 'path-to-validation-data-folder'

#used to generate batches of tensor image data with real-time data augmentation
train_datagen = ImageDataGenerator(
                    rescale=1./255,
                    rotation_range=30,
                    width_shift_range=0.3,
                    height_shift_range=0.3,
                    horizontal_flip=True,
                    fill_mode='nearest')

#resizing validation images
validation_datagen = ImageDataGenerator(rescale=1./255)

#setting batch size
batch_size = 32

#taking data from the specified path in specified batch size for training the model
train_generator = train_datagen.flow_from_directory(
                        directory=train_data_dir,
                        target_size=(img_rows, img_cols),
                        batch_size=batch_size,
                        class_mode="categorical")

#taking data from the specified path in specified batch size for validating the model
validation_generator = validation_datagen.flow_from_directory(
                            directory=validation_data_dir,
                            target_size=(img_rows, img_cols),
                            batch_size=batch_size,
                            class_mode="categorical")


#saving changes after every interval
checkpoint = ModelCheckpoint('model.h5',
                             monitor='val_loss',
                             mode='min',
                             save_best_only=True,
                             verbose=1)

#stopping the training when monitored metric has stopped improving
earlystop = EarlyStopping(
                          monitor='val_loss',
                          min_delta=0,
                          patience=10,
                          verbose=1,
                          restore_best_weights=True)

#reduce learning rate of model if metric stopped improving after certain epochs
learning_rate_reduction = ReduceLROnPlateau(monitor='val_accuracy',
                                            patience=5,
                                            verbose=1,
                                            factor=0.2,
                                            min_lr=0.0001)

#called at each stage of the training
callbacks = [earlystop,checkpoint,learning_rate_reduction]

#configures model for training
model.compile(loss='categorical_crossentropy',
              optimizer=Adam(lr=0.001),
              metrics=['accuracy'])

nb_train_samples = 40045
nb_validation_samples = 11924

epochs = 10

#trains model on fixed number of epochs
history = model.fit(
            train_generator,
            steps_per_epoch=nb_train_samples//batch_size,
            epochs=epochs,
            callbacks=callbacks,
            validation_data=validation_generator,
            validation_steps=nb_validation_samples//batch_size)

acc = history.history['accuracy']
val_acc = history.history['val_accuracy']

loss = history.history['loss']
val_loss = history.history['val_loss']

epochs_range = range(epochs)

plt.figure(figsize=(8, 8))
plt.subplot(1, 2, 1)
plt.plot(epochs_range, acc, label='Training Accuracy')
plt.plot(epochs_range, val_acc, label='Validation Accuracy')
plt.legend(loc='lower right')
plt.title('Training and Validation Accuracy')

plt.subplot(1, 2, 2)
plt.plot(epochs_range, loss, label='Training Loss')
plt.plot(epochs_range, val_loss, label='Validation Loss')
plt.legend(loc='upper right')
plt.title('Training and Validation Loss')
plt.show()

fer_json = model.to_json()
with open("new_model.json", "w") as json_file:
    json_file.write(fer_json)
model.save('./MyModel_tf',save_format='tf')
model.save("new_model.h5")
model.save_weights("new_model.h5")
